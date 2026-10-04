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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI找物助手 业务实现
 *
 * Agent模式: 把AiTools里的工具注册给DeepSeek, 模型自己决定
 * 什么时候查库、什么时候发布, Spring AI负责"模型→工具→模型"的循环,
 * 我们只订阅最终回复的token流, 通过SSE逐段推给小程序
 */
@Service
public class AiServiceImpl implements IAiService {

    @Resource
    private DeepSeekChatModel chatModel;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private CategoryMapper categoryMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    //回复末尾滞留的字符数, 防止[PUBLISH_XXX]标记被流式吐给用户看到
    private static final int HOLD_BACK = 20;
    private static final String MARK_LOST = "[PUBLISH_LOST]";
    private static final String MARK_FOUND = "[PUBLISH_FOUND]";

    //system提示词: 人设 + 工具使用规范 + 输出约定
    private static final String SYSTEM_PROMPT = """
            你是校园失物招领平台的"AI找物助手", 帮同学找回丢失的物品、处理捡到的物品。今天的日期是%s。

            工作规范:
            1. 用户描述丢了什么/捡到什么时, 必须先调用searchItems工具在平台里检索, 再根据结果回答, 不允许凭空编造平台信息。
            2. 检索到相关信息时, 告诉用户找到了几条, 提醒用户点击下方卡片查看详情、仔细核对物品特征后再联系对方。
            3. 检索不到时: 可以主动提出帮用户发布信息。用户同意由你代发布, 就用publishItem;
               用户没同意或没回应发布意愿, 就在回复的最末尾输出标记: 用户丢了东西输出[PUBLISH_LOST], 捡到东西输出[PUBLISH_FOUND],
               标记只能出现在整条回复的结尾, 前面的正文不要提到这个标记。
            4. 代发布(publishItem)前必须收集齐: 物品名称和特征、丢失/拾取地点、日期、联系方式。
               缺什么就追问什么, 严禁编造。收集齐后先完整复述一遍让用户确认, 用户明确同意后才能调用publishItem。
            5. 标记完成(finishMyItem)前必须先用getMyItems查出列表, 和用户确认是哪一条再操作。
            6. 工具返回"用户未登录"时, 引导用户先到小程序底部「我的」页面登录, 再回来继续。
            7. 只聊失物招领相关话题(找物、发布、平台使用等), 无关话题礼貌拒绝并拉回来。
            8. 回复口语化、简洁友好, 控制在150字以内, 不使用表情符号和emoji, 不使用markdown格式。
            """;

    @Override
    public void chatStream(AiChatRequest request, LoginUser loginUser, SseEmitter emitter) {
        //输入防护: 空问题直接引导, 不浪费一次模型调用
        String question = request.getQuestion() == null ? "" : request.getQuestion().trim();
        if (question.isEmpty()) {
            sendDelta(emitter, "你想找什么东西呀? 描述一下物品和丢失地点, 我来帮你在平台里找找。");
            sendDone(emitter, List.of(), null);
            emitter.complete();
            return;
        }
        //超长截断, 防止恶意灌token
        if (question.length() > 200) {
            question = question.substring(0, 200);
        }

        //每次请求new一个工具箱: 隔离本次命中的物品, 同时把登录用户存进去(异步线程里取不到ThreadLocal)
        AiTools aiTools = new AiTools(itemMapper, categoryMapper, loginUser);

        Flux<String> stream;
        try {
            stream = ChatClient.create(chatModel).prompt()
                    .system(String.format(SYSTEM_PROMPT, LocalDate.now()))
                    .messages(toMessages(request.getHistory()))
                    .user(question)
                    .tools(aiTools)
                    .stream()
                    .content();
        } catch (Exception e) {
            sendError(emitter);
            return;
        }

        //尾部滞留缓冲: 始终扣住最后HOLD_BACK个字符不发, 结束时再冲刷并剥离PUBLISH标记
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
                error -> sendError(emitter),
                () -> {
                    //冲刷剩余文本, 剥离引导发布标记转成action
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
        //客户端断开/超时就把上游流掐掉, 不白烧token
        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(() -> {
            disposable.dispose();
            emitter.complete();
        });
    }

    /**
     * 把小程序传来的历史消息转成模型消息(最多取最近10条)
     */
    private List<Message> toMessages(List<AiChatRequest.HistoryMessage> history) {
        List<Message> messages = new ArrayList<>();
        if (history == null || history.isEmpty()) {
            return messages;
        }
        int from = Math.max(0, history.size() - 10);
        for (int i = from; i < history.size(); i++) {
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

    /** 下发一段回复文本增量 */
    private void sendDelta(SseEmitter emitter, String content) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "delta");
        event.put("content", content);
        sendEvent(emitter, event);
    }

    /** 下发结束事件: 携带匹配物品卡片和引导发布action */
    private void sendDone(SseEmitter emitter, List<Item> items, Map<String, String> action) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "done");
        event.put("items", items);
        event.put("action", action);
        sendEvent(emitter, event);
    }

    /** 下发失败降级事件 */
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
            //客户端断开时send会报IO异常, 忽略即可
        }
    }
}
