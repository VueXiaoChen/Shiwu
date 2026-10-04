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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController extends BaseController {

    @Resource
    private IAiService aiService;

    private static final ObjectMapper JSON = new ObjectMapper();

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AiChatRequest request) {
        SseEmitter emitter = new SseEmitter(60_000L);
        aiService.chatStream(request, currentLoginUser(), emitter);
        return emitter;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestParam("image") MultipartFile image) {
        String url = aiService.saveImage(image);
        return Map.of("url", url);
    }

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