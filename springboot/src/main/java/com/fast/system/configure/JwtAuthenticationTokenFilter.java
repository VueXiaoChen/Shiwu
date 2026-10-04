package com.fast.system.configure;

import com.fast.system.domain.LoginUser;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * JWT认证令牌过滤器
 * 作用: 拦截所有HTTP请求, 从请求头中提取JWT token, 验证并且设置用户认证信息
 * 相当于系统的"安检员" - 检查每个访客的通行证(token), 验证通过就放行
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Resource
    private TokenService tokenService;

    /**
     * 过滤器的核心方法(每个请求都会执行)
     * 作用: 从请求头中提取JWT token, 验证用户信息, 设置security认证信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param chain 过滤器链(用于继续执行后续过滤器)
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        //步骤1: 从HTTP请求中解析用户信息
        LoginUser loginUser = tokenService.getLoginUser(request);

        //步骤2: 判断是否需要设置认证信息
        if (Objects.nonNull(loginUser) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                //步骤2.1: 检查token是否已加入黑名单（退出登录后失效）
                String token = tokenService.getToken(request);
                if (tokenService.isTokenBlacklisted(token)) {
                    SecurityContextHolder.clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                //步骤3:验证token是否过期
                tokenService.verifyToken(loginUser);
                //步骤4: 构建security认证令牌
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                //步骤5:设置认证详情
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //步骤6:将认证信息设置到security上下文
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                //步骤7:处理验证失败的情况
                SecurityContextHolder.clearContext();
            }
        }
        //步骤8: 继续执行后续的过滤器
        //无论是否成功执行, 都要继续执行过滤器链
        chain.doFilter(request, response);
    }
}
