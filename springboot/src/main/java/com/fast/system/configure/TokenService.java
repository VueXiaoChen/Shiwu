package com.fast.system.configure;

import com.fast.system.domain.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * token 验证处理
 * 作用: JWT令牌的生成、解析、验证
 * 相当于系统的"身份证制作和检查站"
 */
@Component
@Slf4j
public class TokenService {
    //令牌自定义标识: HTTP请求头中的携带的token字段名
    @Value("${token.header}")
    private String header;

    //令牌密钥 用于加密/解密token的字符串
    @Value("${token.secret}")
    private String secret;

    //令牌有效期
    @Value("${token.expireTime}")
    private int expireTime;

    //JWT签名密钥: 将字符串secret转换成Key对象, 用于签名
    private Key secretKey;

    //JSON处理器: 用于对象与JSON字符串的相互转换
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 系统启动时初始化密钥
     */
    @PostConstruct //在Bean创建完成之后, 依赖注入完成后执行
    public void init() {
        //将配置的令牌密钥字符串转换为 JWT的可用的Key对象
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 创建令牌
     * 功能: 根据用户的信息生成JWT token
     * 使用场景: 用户登录成功之后调用
     */
    public String createToken(LoginUser loginUser) {
        //1.获取当前时间戳
        long now = System.currentTimeMillis();

        //2.计算过期时间(将分钟转换为毫秒)
        long expirationTime = now + TimeUnit.MINUTES.toMillis(expireTime);

        //3.更新用户信息中的时间字段
        loginUser.setLoginTime(now); //登录时间
        loginUser.setExpireTime(expirationTime); //过期时间

        //4.准备JWT的声明数据
        HashMap<String, Object> claims = new HashMap<>();

        //5.将LoginUser对象转换为Json字符串
        try {
            claims.put("user_key", objectMapper.writeValueAsString(loginUser));
        } catch (Exception e) {
            throw new RuntimeException("序列化用户信息失败", e);
        }

        //6.构建JWT token
        return Jwts.builder()
                .setClaims(claims) //设置声明数据
                .setExpiration(new Date(expirationTime)) //设置过期时间
                .signWith(secretKey) //用密钥签名
                .compact(); //生成字符串

    }

    /**
     * 获取用户身份信息
     * 功能: 从HTTP请求中提取token, 解析出用户信息
     * 使用场景: 拦截器中调用, 验证用户是否登录
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        //1.从请求头中获取token字符串
        String token = getToken(request);
        //2.判断token是否为空或者空白
        if (!StringUtils.hasText(token)) {
            return null; //没有token, 说明用户未登录
        }
        try {
            //3.解析token, 获取声明数据
            Claims claims = parseToken(token);
            //4.从声明数据中获取存储的用户JSON字符串
            String userJson = claims.get("user_key", String.class);
            //5.将JSON字符串反序列化为LoginUser对象
            return objectMapper.readValue(userJson, LoginUser.class);
        } catch (Exception e) {
            //6. 捕获所有异常
            log.info("解析用户信息异常=>{}", String.valueOf(e));
        }
        return null; //解析失败返回 null
    }

    /**
     * 从HTTP请求中提取token
     */
    public String getToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /**
     * 解析字符串, 获取声明数据
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) //设置验证密钥
                .build()
                .parseClaimsJws(token) //解析token
                .getBody(); //获取到数据部分
    }

    /**
     * 验证令牌有效期
     */
    public void verifyToken(LoginUser loginUser) {
        //获取过期时间
        Long expireTime = loginUser.getExpireTime();
        //获取当前时间
        long currentTime = System.currentTimeMillis();
        //判断是否已经过去(剩余时间<=0)
        if (expireTime - currentTime <= 0) {
            throw new RuntimeException("Token已过期");
        }
    }

    // ========== Token 黑名单机制（退出登录失效） ==========

    /**
     * 已失效的 Token 黑名单（token -> 过期时间戳）
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * 将 Token 加入黑名单（退出登录时调用）
     *
     * @param token      要失效的 token
     * @param expireTime token 的过期时间戳
     */
    public void invalidateToken(String token, long expireTime) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        blacklistedTokens.put(token, expireTime);
        log.info("Token 已加入黑名单: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }

    /**
     * 检查 Token 是否已被加入黑名单
     *
     * @param token 要检查的 token
     * @return true=已被失效, false=有效
     */
    public boolean isTokenBlacklisted(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        Long expireTime = blacklistedTokens.get(token);
        if (expireTime == null) {
            return false; // 不在黑名单中
        }
        // 如果 token 已自然过期，从黑名单中移除（惰性清理）
        if (expireTime < System.currentTimeMillis()) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true; // 在黑名单中且未过期
    }

}
