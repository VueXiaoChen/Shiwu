package com.fast.system.utils;

import com.fast.system.domain.LoginUser;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全服务工具类
 * 作用: 提供便携的方法获取当前登录用户信息
 * 相当于系统的"身份证读取器" - 快速获取当前用户的身份信息
 *
 * 注意: 这是一个工具类, 所有的方法都是静态的
 *
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户
     * 功能: 从Security的上下文中获取当前已经认证的用户信息
     * @return LoginUser对象, 包含了用户的完整信息
     *
     * 执行流程:
     * 1.SecurityContextHolder.getContext() - 获取Security的上下文
     * 2..getAuthentication() - 获取认证信息
     * 3..getPrincipal() - 获取用户对象
     * 4.(LoginUser) - 强制转换为LoginUser类型
     */
    public static LoginUser getLoginUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 获取当前用户ID (便捷方法)
     * 功能: 快速获取当前登录用户的ID
     * @return 当前用户ID
     */
    public static Long getUserId() {
        //调用getLoginUser()获取用户对象, 然后返回其中的用户ID
        return getLoginUser().getUserId();
    }

}
