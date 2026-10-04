package com.fast.system.domain;

import lombok.Data;

/**
 * 用户注册对象
 */
@Data
public class RegisterBody {
    //用户名
    private String userName;
    //用户密码
    private String password;
}
