package com.fast.ai.controller;

import com.fast.ai.domain.AiChatRequest;
import com.fast.ai.service.IAiService;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.LoginUser;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI找物助手(小程序端)
 */
@RestController
@RequestMapping("/ai")
public class AiController extends BaseController {

    @Resource
    private IAiService aiService;

    /**
     * 流式对话接口: SSE推送 delta/done/error 事件
     * 接口是公开的(未登录也能闲聊找物), 但SSE回复在异步线程里发,
     * 到时候ThreadLocal里的登录态已经没了, 所以这里先把登录用户抓出来传下去
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AiChatRequest request) {
        //超时60秒, 大模型再慢也该说完了
        SseEmitter emitter = new SseEmitter(60_000L);
        //未登录时principal是"anonymousUser"字符串, 转不成LoginUser就当没登录
        LoginUser loginUser = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser lu) {
            loginUser = lu;
        }
        aiService.chatStream(request, loginUser, emitter);
        return emitter;
    }
}
