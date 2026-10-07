package com.fast.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fast.ai.domain.AiChatRequest;
import com.fast.ai.domain.SearchKeyword;
import com.fast.ai.mapper.SearchKeywordMapper;
import com.fast.ai.service.*;
import com.fast.content.mapper.CategoryMapper;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
import com.fast.system.config.fastConfig;
import com.fast.system.domain.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI找物助手 业务实现
 * 方法三改造：上传图片时后端直接跑检索，把结果作为上下文给 AI，AI 只总结不决策。
 * 统一路径：{profile}/file/upload/
 */
@Slf4j
@Service
public class AiServiceImpl implements IAiService {

    @Resource
    private SearchKeywordMapper searchKeywordMapper;

    @Resource
    private ImageSearchService imageSearchService;

    @Resource
    private FaceFeatureService faceFeatureService;

    @Resource
    private ImageFeatureService imageFeatureService;

    @Resource
    private OpenAiChatModel chatModel;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private fastConfig fastConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int HOLD_BACK = 20;
    private static final String MARK_LOST = "[PUBLISH_LOST]";
    private static final String MARK_FOUND = "[PUBLISH_FOUND]";

    // 对外可访问的图片前缀（统一 /file/upload/）
    private static final String IMAGE_PUBLIC_PREFIX = "/file/upload/";

    private static final String SYSTEM_PROMPT = """
        你是校园失物招领平台的"AI找物助手", 帮同学找回丢失的物品、处理捡到的物品。今天的日期是%s。

        平台当前支持的特殊关键词：%s

        工作规范:
        1. 用户描述丢了什么/捡到什么，或输入以上关键词时，必须先调用 searchItems 工具在平台里检索。
           关键词可以是物品词(耳机/AirPods)或人物特征词(美女/帅哥)，地点词一律放 location 参数。
           不要在回复里直接编造平台数据。
        2. 用户上传图片时，如果系统已经在消息里提供了【图片检索结果】，你只需要基于这个结果总结回答，
           不要再调用 searchByImageVector 工具，也不要因为图片里有人脸就拒绝回答。
           如果【图片检索结果】为空，就如实告诉用户没找到，并主动提出帮用户发布信息。
        3. 检索到相关信息时，告诉用户找到了几条，提醒用户点击下方卡片查看详情、核对特征后再联系对方。
        4. 检索不到时，可以主动提出帮用户发布信息。
        5. 代发布(publishItem)前必须收集齐: 物品名称和特征、地点、日期、联系方式。
           如果涉及人物描述，要在 personTags 参数里填入人物特征标签(如 美女,长发)。
           收集齐后先完整复述一遍让用户确认，用户明确同意后才能调用 publishItem。
        6. 标记完成(finishMyItem)前必须先用 getMyItems 查出列表, 和用户确认是哪一条再操作。
        7. 工具返回"用户未登录"时, 引导用户先到小程序底部「我的」页面登录。
        8. 只聊失物招领相关话题。
        9. 回复口语化、简洁友好, 控制在150字以内, 不使用表情符号和emoji, 不使用markdown格式。
        """;

    @Override
    public void chatStream(AiChatRequest request, LoginUser loginUser, SseEmitter emitter) {
        String question = request.getQuestion() == null ? "" : request.getQuestion().trim();

        // 1. 处理图片
        String imageUrl = request.getImageUrl();
        if (imageUrl == null && request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            try {
                imageUrl = saveImage(request.getImageFile());
                request.setImageUrl(imageUrl);
            } catch (Exception e) {
                log.error("图片保存失败", e);
                sendDelta(emitter, "图片保存失败了, 请重新上传试试。");
                sendDone(emitter, List.of(), null);
                emitter.complete();
                return;
            }
        }
        boolean hasImage = imageUrl != null && !imageUrl.isBlank();

        if (question.isEmpty() && !hasImage) {
            sendDelta(emitter, "你想找什么东西呀? 描述一下物品和丢失地点, 或者上传一张图片, 我来帮你在平台里找找。");
            sendDone(emitter, List.of(), null);
            emitter.complete();
            return;
        }
        if (question.length() > 200) {
            question = question.substring(0, 200);
        }

        log.info("AI对话开始: question={}, hasImage={}, imageUrl={}", question, hasImage, imageUrl);

        // ★ 有图片时，后端直接跑检索
        final List<Item> preSearchHits = new ArrayList<>();
        String imageSearchContext = "";
        if (hasImage) {
            try {
                String diskPath = urlToDiskPath(imageUrl);
                List<Item> candidates = itemMapper.selectList(
                        new LambdaQueryWrapper<Item>()
                                .eq(Item::getStatus, "open")
                                .orderByDesc(Item::getCreateTime)
                                .last("limit 500"));
                List<Item> hits = imageSearchService.search(diskPath, candidates);
                preSearchHits.addAll(hits.stream().limit(10).toList());
                imageSearchContext = buildSearchContext(preSearchHits);
                log.info("主动图片检索完成: diskPath={}, 命中={} 条", diskPath, preSearchHits.size());
            } catch (Exception e) {
                log.error("主动图片检索失败", e);
                imageSearchContext = "【图片检索结果】检索过程出错，请如实告知用户暂时无法检索。\n";
            }
        }

        // 2. 工具箱
        AiTools aiTools = new AiTools(
                itemMapper,
                categoryMapper,
                searchKeywordMapper,
                loginUser,
                imageUrl,
                imageSearchService,
                faceFeatureService,
                imageFeatureService
        );

        Flux<String> stream;
        try {
            List<Message> messages = new ArrayList<>();
            String kwStr = loadKeywords();
            messages.add(new SystemMessage(
                    String.format(SYSTEM_PROMPT, LocalDate.now(), kwStr)));
            messages.addAll(toMessages(request.getHistory()));
            messages.add(buildUserMessage(question, imageUrl, imageSearchContext));

            stream = ChatClient.create(chatModel).prompt()
                    .messages(messages)
                    .tools(aiTools)
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("AI流启动失败", e);
            sendError(emitter);
            return;
        }

        // 3. 尾部滞留 + SSE 推送
        StringBuilder pending = new StringBuilder();
        Disposable disposable = stream.subscribe(
                token -> {
                    pending.append(token);
                    if (pending.length() > HOLD_BACK) {
                        String sendable = pending.substring(0, pending.length() - HOLD_BACK);
                        pending.delete(0, pending.length() - HOLD_BACK);
                        sendDelta(emitter, sendable);
                    }
                },
                error -> {
                    log.error("AI流执行出错", error);
                    sendError(emitter);
                },
                () -> {
                    String tail = pending.toString();
                    Map<String, String> action = null;
                    if (tail.contains(MARK_LOST)) {
                        tail = tail.replace(MARK_LOST, "");
                        action = buildAction("lost", "去发布寻物启事");
                    } else if (tail.contains(MARK_FOUND)) {
                        tail = tail.replace(MARK_FOUND, "");
                        action = buildAction("found", "去发布失物招领");
                    }
                    tail = tail.stripTrailing();
                    if (!tail.isEmpty()) {
                        sendDelta(emitter, tail);
                    }
                    List<Item> merged = new ArrayList<>(preSearchHits);
                    for (Item it : aiTools.getMatchedItems()) {
                        boolean exists = false;
                        for (Item x : merged) {
                            if (x.getItemId().equals(it.getItemId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            merged.add(it);
                        }
                    }
                    List<Item> finalItems = merged.stream().limit(50).toList();
                    sendDone(emitter, finalItems, action);
                    emitter.complete();
                }
        );
        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(() -> {
            disposable.dispose();
            emitter.complete();
        });
    }

    /**
     * URL 转磁盘路径，统一用 {profile}/file/upload/
     */
    private String urlToDiskPath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        return Paths.get(fastConfig.getProfile(), "file", "upload", fileName)
                .normalize()
                .toString();
    }

    /**
     * 把检索结果拼成文本上下文给 AI
     */
    private String buildSearchContext(List<Item> hits) {
        if (hits == null || hits.isEmpty()) {
            return "【图片检索结果】未在平台找到相似的失物/招领信息。\n";
        }
        SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder("【图片检索结果】根据图片特征，找到以下 ")
                .append(hits.size()).append(" 条可能相关的信息：\n");
        for (Item it : hits) {
            sb.append("- itemId=").append(it.getItemId())
                    .append(", 类型=").append("found".equals(it.getType()) ? "失物招领" : "寻物启事")
                    .append(", 标题=").append(it.getTitle() == null ? "" : it.getTitle())
                    .append(", 地点=").append(it.getLocation() == null ? "" : it.getLocation())
                    .append(", 时间=").append(it.getHappenTime() == null ? "" : day.format(it.getHappenTime()))
                    .append("\n");
        }
        sb.append("请基于以上结果总结回答用户，不要再调用 searchByImageVector 工具。\n");
        return sb.toString();
    }

    private String loadKeywords() {
        List<SearchKeyword> list = searchKeywordMapper.selectList(
                new LambdaQueryWrapper<SearchKeyword>()
                        .eq(SearchKeyword::getEnabled, 1)
                        .orderByAsc(SearchKeyword::getSortOrder));
        if (list.isEmpty()) return "（暂无配置）";
        return list.stream()
                .map(SearchKeyword::getKeyword)
                .collect(Collectors.joining("、"));
    }

    /**
     * 组装用户消息，把检索上下文拼进去
     */
    private UserMessage buildUserMessage(String text, String imageUrl, String searchContext) {
        String baseText = (text == null || text.isBlank())
                ? "请帮我找找图片里的这个物品"
                : text;
        String finalText = (searchContext == null ? "" : searchContext) + "\n用户问题：" + baseText;

        if (imageUrl == null || imageUrl.isBlank()) {
            return new UserMessage(finalText);
        }

        try {
            String diskPath = urlToDiskPath(imageUrl);
            byte[] bytes = Files.readAllBytes(Paths.get(diskPath));

            String mimeType = guessMimeType(imageUrl);
            Media media = new Media(MimeType.valueOf(mimeType), new ByteArrayResource(bytes));

            log.info("构造多模态消息: imageUrl={}, 大小={} bytes, mime={}",
                    imageUrl, bytes.length, mimeType);

            return UserMessage.builder()
                    .text(finalText)
                    .media(media)
                    .build();
        } catch (IOException e) {
            log.error("读取图片失败: {}", imageUrl, e);
            return new UserMessage(finalText + "\n(图片读取失败, 请让用户重传)");
        }
    }

    private String guessMimeType(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    /**
     * 保存图片，统一到 {profile}/file/upload/，返回 URL /file/upload/xxx
     */
    @Override
    public String saveImage(MultipartFile file) {
        try {
            Path dir = Paths.get(fastConfig.getProfile(), "file", "upload")
                    .toAbsolutePath().normalize();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(fileName);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String url = IMAGE_PUBLIC_PREFIX + fileName;
            log.info("图片已保存: 磁盘={}, URL={}", target, url);
            return url;
        } catch (IOException e) {
            log.error("保存图片失败", e);
            throw new RuntimeException("图片保存失败", e);
        }
    }

    private List<Message> toMessages(List<AiChatRequest.HistoryMessage> history) {
        List<Message> messages = new ArrayList<>();
        if (history == null || history.isEmpty()) return messages;

        int end = history.size();
        if (end > 0 && "assistant".equals(history.get(end - 1).getRole())) {
            end--;
        }
        int from = Math.max(0, end - 10);

        for (int i = from; i < end; i++) {
            AiChatRequest.HistoryMessage h = history.get(i);
            if (h == null || h.getContent() == null || h.getContent().isBlank()) continue;
            if ("assistant".equals(h.getRole())) {
                messages.add(new AssistantMessage(h.getContent()));
            } else {
                messages.add(new UserMessage(h.getContent()));
            }
        }
        return messages;
    }

    private Map<String, String> buildAction(String publishType, String label) {
        Map<String, String> action = new LinkedHashMap<>();
        action.put("publishType", publishType);
        action.put("label", label);
        return action;
    }

    private void sendDelta(SseEmitter emitter, String content) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "delta");
        event.put("content", content);
        sendEvent(emitter, event);
    }

    private void sendDone(SseEmitter emitter, List<Item> items, Map<String, String> action) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "done");
        event.put("items", items);
        event.put("action", action);
        sendEvent(emitter, event);
    }

    private void sendError(SseEmitter emitter) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "error");
        event.put("msg", "AI 服务暂时不可用，请稍后再试");
        sendEvent(emitter, event);
        emitter.complete();
    }

    private void sendEvent(SseEmitter emitter, Map<String, Object> event) {
        try {
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(event)));
        } catch (Exception e) {
            // 客户端断开时忽略
        }
    }
}