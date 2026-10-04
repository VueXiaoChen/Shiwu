package com.fast.ai.domain;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AiChatRequest {
    private String question;
    private List<HistoryMessage> history;

    // 方式一: 前端先调 /ai/upload 拿到 url, 走 JSON 传过来
    private String imageUrl;
    // 方式二: 直接 multipart 上传(兼容 /chat/with-image)
    private transient MultipartFile imageFile;

    @Data
    public static class HistoryMessage {
        private String role;
        private String content;
    }
}