package com.fast.ai.controller;

import com.fast.ai.domain.AiChatRequest;
import com.fast.ai.service.IAiService;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.LoginUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI找物助手(小程序端)
 */
@RestController
@RequestMapping("/ai")
public class AiController extends BaseController {

    @Resource
    private IAiService aiService;

    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * 纯文字流式对话: SSE 推送 delta/done/error 事件
     * 带图片时也走这里, imageUrl 由前端先调 /ai/upload 拿到后一并传过来
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AiChatRequest request) {
        SseEmitter emitter = new SseEmitter(60_000L);
        aiService.chatStream(request, currentLoginUser(), emitter);
        return emitter;
    }

    /**
     * 图片上传: 一次性返回图片的可访问 URL
     * 前端拿到 url 后再调 /ai/chat 走流式对话, 避开 wx.uploadFile 不支持 SSE 的问题
     *
     * form-data 字段:
     *  - image 图片文件(必填)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestParam("image") MultipartFile image) {
        String url = aiService.saveImage(image);
        return Map.of("url", url);
    }

    /**
     * 兼容旧版: 保留 /chat/with-image (如前端还在用)
     * 内部做一次转存, 然后走同一套 chatStream
     */
    @PostMapping(value = "/chat/with-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatWithImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "question", required = false) String question,
            @RequestParam(value = "history", required = false) String historyJson) {

        SseEmitter emitter = new SseEmitter(60_000L);

        AiChatRequest request = new AiChatRequest();
        request.setQuestion(question == null || question.isBlank()
                ? "请帮我找找图片里的这个物品" : question);
        request.setHistory(parseHistory(historyJson));
        request.setImageFile(image);

        aiService.chatStream(request, currentLoginUser(), emitter);
        return emitter;
    }

    private LoginUser currentLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser lu) {
            return lu;
        }
        return null;
    }

    private List<AiChatRequest.HistoryMessage> parseHistory(String historyJson) {
        if (historyJson == null || historyJson.isBlank()) return Collections.emptyList();
        try {
            return JSON.readValue(historyJson,
                    new TypeReference<List<AiChatRequest.HistoryMessage>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}