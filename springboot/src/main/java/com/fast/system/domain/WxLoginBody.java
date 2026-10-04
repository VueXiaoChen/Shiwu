package com.fast.system.domain;

import lombok.Data;

/**
 * 微信小程序登录请求体
 * 
 * 前端小程序调用 wx.login() 获取 code，
 * 再调用 wx.getUserProfile() 获取用户昵称头像，
 * 把这两样东西一起发给后端的 /wx-login 接口
 */
@Data
public class WxLoginBody {

    /** wx.login() 返回的临时登录凭证，只能用一次，有效期 5 分钟 */
    private String code;

    /** wx.getUserProfile() 拿到的微信用户信息 */
    private WxUserInfo userInfo;

    /**
     * 微信用户信息
     */
    @Data
    public static class WxUserInfo {
        /** 微信昵称 */
        private String nickName;
        /** 微信头像 URL */
        private String avatarUrl;
        /** 性别：0-未知 1-男 2-女 */
        private Integer gender;
    }
}
