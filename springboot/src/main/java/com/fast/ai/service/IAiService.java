package com.fast.ai.service;

import com.fast.ai.domain.AiChatRequest;
import com.fast.system.domain.LoginUser;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI找物助手 业务接口
 */
public interface IAiService {

    /**
     * 流式对话: AI回复通过SSE逐段推给小程序
     * 支持纯文字(request.imageFile==null && request.imageUrl==null)
     * 和带图片(request.imageFile!=null 或 request.imageUrl!=null)两种模式
     */
    void chatStream(AiChatRequest request, LoginUser loginUser, SseEmitter emitter);

    /**
     * 保存图片, 返回对外可访问的 URL
     * 供 /ai/upload 端点独立调用
     */
    String saveImage(MultipartFile file);
}