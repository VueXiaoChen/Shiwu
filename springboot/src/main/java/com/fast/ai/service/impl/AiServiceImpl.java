package com.fast.ai.service.impl;

import com.fast.ai.domain.AiChatRequest;
import com.fast.ai.service.AiTools;
import com.fast.ai.service.IAiService;
import com.fast.content.mapper.CategoryMapper;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI找物助手 业务实现
 *
 * 用 OpenAiChatModel(走 OpenAI 兼容协议) 指向 DeepSeek,
 * 这样 UserMessage 里塞的 Media(图片) 才能被正确序列化传给模型
 */
@Slf4j
@Service
public class AiServiceImpl implements IAiService {

    @Resource
    private OpenAiChatModel chatModel;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private CategoryMapper categoryMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    //尾部滞留字符数
    private static final int HOLD_BACK = 20;
    private static final String MARK_LOST = "[PUBLISH_LOST]";
    private static final String MARK_FOUND = "[PUBLISH_FOUND]";

    //图片本地存储目录: 项目运行目录下的 upload/ai, 跨平台安全
    private static final String IMAGE_UPLOAD_DIR = "upload/ai/";
    //对外可访问的图片前缀(需和静态资源映射/Nginx 对齐)
    private static final String IMAGE_PUBLIC_PREFIX = "/upload/ai/";

    private static final String SYSTEM_PROMPT = """
            你是校园失物招领平台的"AI找物助手", 帮同学找回丢失的物品、处理捡到的物品。今天的日期是%s。

            工作规范:
            1. 用户描述丢了什么/捡到什么时, 必须先调用searchItems工具在平台里检索, 再根据结果回答, 不允许凭空编造平台信息。
            2. 用户上传图片时, 先仔细识别图片里的物品(品类、颜色、品牌、形状、材质等特征),
               再调用searchByImage工具, 把识别出的特征词作为keywords传入检索, 不要跳过工具直接回答。
               识别特征词时按顺序: 品类(耳机/水杯/钱包) > 品牌(AirPods/小米) > 颜色(黑色/白色) > 形状材质(入耳式/不锈钢)。
               若图片模糊无法识别, 直接告诉用户"图片看不清, 请补充描述", 不要瞎猜关键词。
            3. 检索到相关信息时, 告诉用户找到了几条, 提醒用户点击下方卡片查看详情、仔细核对物品特征后再联系对方。
            4. 检索不到时: 可以主动提出帮用户发布信息。用户同意由你代发布, 就用publishItem;
               用户没同意或没回应发布意愿, 就在回复的最末尾输出标记: 用户丢了东西输出[PUBLISH_LOST], 捡到东西输出[PUBLISH_FOUND],
               标记只能出现在整条回复的结尾, 前面的正文不要提到这个标记。
            5. 代发布(publishItem)前必须收集齐: 物品名称和特征、丢失/拾取地点、日期、联系方式。
               缺什么就追问什么, 严禁编造。收集齐后先完整复述一遍让用户确认, 用户明确同意后才能调用publishItem。
            6. 标记完成(finishMyItem)前必须先用getMyItems查出列表, 和用户确认是哪一条再操作。
            7. 工具返回"用户未登录"时, 引导用户先到小程序底部「我的」页面登录, 再回来继续。
            8. 只聊失物招领相关话题(找物、发布、平台使用等), 无关话题礼貌拒绝并拉回来。
            9. 回复口语化、简洁友好, 控制在150字以内, 不使用表情符号和emoji, 不使用markdown格式。
            """;

    @Override
    public void chatStream(AiChatRequest request, LoginUser loginUser, SseEmitter emitter) {
        String question = request.getQuestion() == null ? "" : request.getQuestion().trim();

        //1. 处理图片: 兼容两种入参(imageFile 直接上传 / imageUrl 已上传后的URL)
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

        //输入防护
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

        //2. 工具箱
        AiTools aiTools = new AiTools(itemMapper, categoryMapper, loginUser, imageUrl);

        Flux<String> stream;
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(String.format(SYSTEM_PROMPT, LocalDate.now())));
            messages.addAll(toMessages(request.getHistory()));
            // ★ 关键: 走 buildUserMessage, 有图就把图片二进制塞进 Media 真正传给模型
            messages.add(buildUserMessage(question, imageUrl));

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

        //3. 尾部滞留 + SSE 推送
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
                    sendDone(emitter, aiTools.getMatchedItems(), action);
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
     * 组装本轮用户消息: 有图片就把图片二进制塞进 Media, 真正传给模型
     */
    private UserMessage buildUserMessage(String text, String imageUrl) {
        String finalText = (text == null || text.isBlank())
                ? "请帮我找找图片里的这个物品"
                : text;

        if (imageUrl == null || imageUrl.isBlank()) {
            return new UserMessage(finalText);
        }

        try {
            // 从保存的 URL 反推磁盘路径
            Path imgPath = Paths.get(IMAGE_UPLOAD_DIR)
                    .toAbsolutePath().normalize()
                    .resolve(imageUrl.substring(imageUrl.lastIndexOf("/") + 1));
            byte[] bytes = Files.readAllBytes(imgPath);

            String mimeType = guessMimeType(imageUrl);
            Media media = new Media(MimeType.valueOf(mimeType), new ByteArrayResource(bytes));

            log.info("构造多模态消息: imageUrl={}, 大小={} bytes, mime={}",
                    imageUrl, bytes.length, mimeType);

            return UserMessage.builder()
                    .text(finalText + "\n请先仔细识别图片里的物品特征(品类/颜色/品牌/形状), 再用 searchByImage 工具检索")
                    .media(media)
                    .build();
        } catch (IOException e) {
            log.error("读取图片失败: {}", imageUrl, e);
            return new UserMessage(finalText + "\n(图片读取失败, 请让用户重传)");
        }
    }

    /**
     * 按扩展名猜 MIME 类型
     */
    private String guessMimeType(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    /**
     * 保存图片, 返回对外可访问的 URL
     */
    @Override
    public String saveImage(MultipartFile file) {
        try {
            Path dir = Paths.get(IMAGE_UPLOAD_DIR).toAbsolutePath().normalize();
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

    /**
     * 历史消息转模型消息(保证最后一条不是 assistant)
     */
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