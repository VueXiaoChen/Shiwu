package com.fast.system.domain;

import lombok.Data;

/**
 * 用户登录对象
 */
@Data
public class LoginBody {
    //用户名
    private String userName;
    //用户密码
    private String password;
}
