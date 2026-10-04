package com.fast.system.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 登录用户身份类
 * 作用: 封装登录用户的信息,
 * 相当于系统内部的"用户身份证"
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {
    //用户ID:唯一标识
    private Long userId;

    //登录时间: 用户登录的时间戳 (毫秒)
    private Long loginTime;

    //过期时间: token的过期时间戳
    private Long expireTime;

    //用户信息: 关联的User实体信息
    private User user;

    //带参数的构造方法
    public LoginUser(Long userId, User user) {
        this.userId = userId;
        this.user = user;
    }

    /**
     * 获取用户的权限集合
     * @return null
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
     * 获取用户的密码
     * @return 用户的密码
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 获取用户的用户名
     * @return 用户的用户名
     */
    @Override
    public String getUsername() {
        return user.getUserName();
    }
}
