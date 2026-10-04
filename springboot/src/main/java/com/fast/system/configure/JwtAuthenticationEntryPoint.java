package com.fast.system.configure;

import com.fast.system.domain.AjaxResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 认证入口点处理类
 * 作用: 当用户访问受保护接口但是未认证(比如没有登录)时, 同意返回JSON格式的错误信息
 * 当当与系统的"门卫" - 当方可没有通行证时, 礼貌的告诉他"请登录"
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 认证入口点方法
     * 当用户访问需要认证的接口但是未认证时, 我们的Security会自动调用这个方法
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param e 认证异常对象 (包含具体的错误信息)
     * @throws IOException IO异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        //1. 设置HTTP状态码为401 (未认证)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        //2. 设置响应内容为JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        //3. 设置字符编码为UTF-8 (避免中文乱码)
        response.setCharacterEncoding("UTF-8");

        //4. 构建统一的错误响应
        String jsonResponse = objectMapper.writeValueAsString(AjaxResult.error(401, "登录已过期, 请重新登录"));

        //5. 将JSON响应数据写入HTTP响应体
        response.getWriter().println(jsonResponse);
    }
}
