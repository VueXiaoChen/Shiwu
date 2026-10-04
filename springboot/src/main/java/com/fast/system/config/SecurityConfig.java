package com.fast.system.config;

import com.fast.system.configure.JwtAuthenticationEntryPoint;
import com.fast.system.configure.JwtAuthenticationTokenFilter;
import com.fast.system.configure.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * spring security 安全配置类
 * 这个类就像是整个系统的"保安队长", 负责配置系统的安全规则
 * 1.哪些门可以自由出入(公开接口)
 * 2.哪些门需要检查证件(需要登录)
 * 3.如何检查证件(JWT认证)
 * 4.发现可疑人员如何处理(异常处理)
 */
@Slf4j
@EnableMethodSecurity(securedEnabled = true) //启用方法级别的安全控制(就像给每个房间再加了一把锁)
@Configuration
public class SecurityConfig {

    @Resource
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Resource
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    @Resource
    private TokenService tokenService;

    /**
     * 安全过滤器链 - 整个安保系统的核心配置
     * 1.跨域检查 -> 2.防止CSRF防护 -> 3.处理异常 -> 4.路径的权限检查 -> 5.JWT验证
     *
     * @param http spring security提供的配置工具
     * @return 配置好的安全过滤器链
     * @throws Exception 异常
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //1.开启跨域支持(允许前端应用访问)
                .cors(Customizer.withDefaults())

                //2.禁用CSRF防护(因为我们是前后端分离, 用JWT进行认证就行了)
                //解释: 这个防护是防止网站被恶意利用的安全机制
                //但是前后端分离 + JWT认证的情况下, 可以关闭, 因为每次请求都携带token
                .csrf(AbstractHttpConfigurer::disable)

                //3.响应头配置 - 防止点击劫持攻击的工具
                // 就像告诉浏览器: "你不要让别人把我家的网页嵌入到他们的网页里"
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                //4.禁用Session - 使用无状态认证(JWT)
                // 每次请求都独立验证, 不需要记住用户的登录状态
                //好处: 容易拓展, 多个服务器之间不需要同步session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //5.异常处理 - 当认证失败时的处理方式
                //比如: 没有带token, token过期, token无效等情况
                //就像是安检员发现有人想蒙混过关时的处理刘嫦娥
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint))

                //6.路径权限配置 - 定义哪些路径需要认证, 哪些路径不需要认证
                .authorizeHttpRequests(req -> req
                        //公开接口 - 所有人都可以访问
                        .requestMatchers("/login", "/register", "/wx-login", "/profile/**").permitAll()
                        //公告查询接口公开 - 小程序未登录也能浏览公告
                        .requestMatchers("/content/notice/selectNoticeList", "/content/notice/selectNoticeByNoticeId/**").permitAll()
                        //分类查询接口公开 - 小程序未登录也能浏览分类
                        .requestMatchers("/content/category/selectCategoryAll").permitAll()
                        //物品浏览类接口公开 - 小程序未登录也能刷列表/看详情/搜索
                        .requestMatchers("/item/item/selectItemList", "/item/item/selectItemByItemId/**",
                                "/item/item/selectItemStats", "/item/item/increaseViews/**").permitAll()
                        // AI 找物助手：纯文字 + 带图片两个端点都放行
                        .requestMatchers("/ai/chat", "/ai/chat/with-image").permitAll()
                        .requestMatchers("/login", "/register").permitAll()
                        //其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                //7.退出登录处理
                .logout(logout -> logout.logoutUrl("/logout")
                        //退出成功之后 - 将token加入黑名单使其失效，返回200状态码
                        .logoutSuccessHandler((req, res, auth) -> {
                            // 获取当前请求携带的 token
                            String token = tokenService.getToken(req);
                            if (StringUtils.hasText(token)) {
                                try {
                                    // 解析 token 的过期时间
                                    Claims claims = tokenService.parseToken(token);
                                    Date expiration = claims.getExpiration();
                                    // 将 token 加入黑名单
                                    tokenService.invalidateToken(token, expiration.getTime());
                                    log.info("用户退出登录，Token 已失效");
                                } catch (Exception e) {
                                    log.warn("退出登录时处理 Token 失败: {}", e.getMessage());
                                }
                            }
                            res.setStatus(HttpServletResponse.SC_OK);
                        })
                )

                //8.添加JWT的过滤器 - 在用户妈妈过滤器之前执行
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

}
