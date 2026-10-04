package com.fast.ai.service;

import com.fast.ai.domain.AiChatRequest;
import com.fast.system.domain.LoginUser;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IAiService {
    void chatStream(AiChatRequest request, LoginUser loginUser, SseEmitter emitter);
    String saveImage(MultipartFile file);
}