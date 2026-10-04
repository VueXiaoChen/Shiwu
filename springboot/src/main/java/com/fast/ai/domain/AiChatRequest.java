package com.fast.ai.domain;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI找物助手 对话请求参数
 */
@Data
public class AiChatRequest {
    //用户本轮输入的问题
    private String question;
    //最近几轮的对话上下文
    private List<HistoryMessage> history;

    //=== 图片相关(二选一) ===
    //方式一: 前端先调 /ai/upload 拿到 url, 走 JSON 传过来
    private String imageUrl;
    //方式二: 直接 multipart 上传(兼容 /chat/with-image)
    private transient MultipartFile imageFile;

    @Data
    public static class HistoryMessage {
        private String role;
        private String content;
    }
}