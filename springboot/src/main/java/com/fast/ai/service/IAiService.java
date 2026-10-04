package com.fast.ai.service;

import com.fast.ai.domain.AiChatRequest;
import com.fast.system.domain.LoginUser;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI找物助手 业务接口
 */
public interface IAiService {

    /**
     * 流式对话: AI回复通过SSE逐段推给小程序
     * @param request 对话请求(问题+历史)
     * @param loginUser 当前登录用户(未登录为null)
     * @param emitter SSE发射器
     */
    void chatStream(AiChatRequest request, LoginUser loginUser, SseEmitter emitter);
}
