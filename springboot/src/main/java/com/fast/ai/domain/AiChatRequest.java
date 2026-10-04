package com.fast.ai.domain;

import lombok.Data;

import java.util.List;

/**
 * AI找物助手 对话请求参数
 */
@Data
public class AiChatRequest {
    //用户本轮输入的问题
    private String question;
    //最近几轮的对话上下文(小程序端传过来, 让AI能接得上话茬)
    private List<HistoryMessage> history;

    /**
     * 历史消息: role=user用户说的 / assistant是AI说的
     */
    @Data
    public static class HistoryMessage {
        private String role;
        private String content;
    }
}
