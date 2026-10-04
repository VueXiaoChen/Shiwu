package com.fast.system.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fast.system.configure.TokenService;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.User;
import com.fast.system.domain.WxLoginBody;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * 微信小程序登录控制器
 * 
 * 处理微信一键登录的完整流程：
 * 1. 接收小程序发来的 code + 用户信息
 * 2. 拿 code 去微信服务器换 openId（这是微信用户的唯一标识）
 * 3. 根据 openId 查找或创建系统用户
 * 4. 生成 JWT Token 返回给小程序
 */
@Slf4j
@RestController
public class WxLoginController extends BaseController {

    @Resource
    private IUserService userService;

    @Resource
    private TokenService tokenService;

    @Resource
    private ObjectMapper objectMapper;

    /** 微信小程序 AppID（在 application.yml 中配置） */
    @Value("${wx.appid}")
    private String appId;

    /** 微信小程序 AppSecret（在 application.yml 中配置） */
    @Value("${wx.secret}")
    private String secret;

    /**
     * 微信小程序登录接口
     * 
     * 前端调用 wx.login() 获取临时 code，再调用 wx.getUserProfile() 获取用户信息，
     * 把这两样一起 POST 到这个接口
     * 
     * @param wxLoginBody 包含 code 和 userInfo
     * @return 登录成功返回 JWT token + 用户信息
     */
    @PostMapping("/wx-login")
    public AjaxResult wxLogin(@RequestBody WxLoginBody wxLoginBody) {
        // 步骤1：校验参数
        if (wxLoginBody.getCode() == null || wxLoginBody.getCode().isEmpty()) {
            return error("登录凭证 code 不能为空");
        }

        // 步骤2：拿 code 去微信服务器换 openId
        String openId;
        try {
            openId = getOpenIdFromWechat(wxLoginBody.getCode());
        } catch (Exception e) {
            log.error("调用微信接口换取 openId 失败", e);
            return error("微信登录失败，请重试");
        }

        if (openId == null || openId.isEmpty()) {
            return error("获取微信身份信息失败");
        }

        log.info("微信登录：openId = {}", openId);

        // 步骤3：根据 openId 查找用户，没有就创建一个新的
        User user = userService.selectUserByWxOpenId(openId);
        boolean isNewUser = false;

        if (user == null) {
            // 新用户 —— 自动注册
            user = createUserFromWechat(openId, wxLoginBody.getUserInfo());
            userService.registerUser(user);
            isNewUser = true;
            log.info("微信新用户注册成功：{}", user.getUserName());
        } else {
            // 老用户 —— 更新昵称和头像（微信那边可能换了）
            updateWechatUserInfo(user, wxLoginBody.getUserInfo());
        }

        // 步骤4：生成 JWT Token
        LoginUser loginUser = new LoginUser(user.getUserId(), user);
        String token = tokenService.createToken(loginUser);

        // 步骤5：返回结果
        AjaxResult result = success().put("token", token);
        result.put("isNewUser", isNewUser);
        // 把用户信息也一起返回，省得前端再调一次 getInfo
        user.setPassword(null); // 密码绝对不能返回！
        result.put("user", user);
        return result;
    }

    /**
     * 调用微信官方接口，用 code 换取 openId
     * 
     * 微信接口地址：https://api.weixin.qq.com/sns/jscode2session
     * 返回示例：{"openid":"xxx","session_key":"xxx","unionid":"xxx"}
     */
    private String getOpenIdFromWechat(String code) throws Exception {
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, secret, code
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("微信接口返回异常状态码：" + response.statusCode());
        }

        JsonNode json = objectMapper.readTree(response.body());

        // 微信返回错误时会有 errcode 字段
        if (json.has("errcode") && json.get("errcode").asInt() != 0) {
            String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : "未知错误";
            throw new RuntimeException("微信接口返回错误：" + errMsg);
        }

        return json.has("openid") ? json.get("openid").asText() : null;
    }

    /**
     * 根据微信信息创建一个新的系统用户
     * 
     * 生成规则：
     * - 用户名：wx_ + 随机短串（保证唯一）
     * - 密码：随机生成（微信登录不检验密码，但数据库不能为空）
     * - 头像、昵称：直接用微信的
     * - 角色：默认普通用户（roleId = 2）
     */
    private User createUserFromWechat(String openId, WxLoginBody.WxUserInfo wxUserInfo) {
        User user = new User();

        // 用户名：wx_ + UUID 前 8 位，保证唯一
        String shortId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        user.setUserName("wx_" + shortId);

        // 密码：随机生成一段，用户不会用到密码登录
        user.setPassword(UUID.randomUUID().toString());

        // 微信信息
        user.setWxOpenId(openId);
        if (wxUserInfo != null) {
            user.setAvatar(wxUserInfo.getAvatarUrl());
            // 把微信昵称存到 userName 也可以，但 userName 已被占用作登录名
            // 这里我们选择把昵称存储方式改为：保留微信昵称在 avatar 旁
            if (wxUserInfo.getNickName() != null && !wxUserInfo.getNickName().isEmpty()) {
                // 微信昵称存入备注字段... 但目前 user 表没有 nickname 字段
                // 实际项目中建议加一个 nick_name 字段，这里我们约定：前端展示用微信返回的昵称
            }
            // 性别转换：微信 gender: 0-未知 1-男 2-女
            if (wxUserInfo.getGender() != null) {
                user.setSex(wxUserInfo.getGender() == 1 ? 0 : (wxUserInfo.getGender() == 2 ? 1 : null));
            }
        }

        // 默认角色：普通用户
        user.setRoleId(2L);

        return user;
    }

    /**
     * 老用户再次登录时，更新微信头像和昵称
     * （用户可能在微信里换了头像和昵称）
     */
    private void updateWechatUserInfo(User user, WxLoginBody.WxUserInfo wxUserInfo) {
        if (wxUserInfo == null) {
            return;
        }
        boolean needUpdate = false;

        if (wxUserInfo.getAvatarUrl() != null && !wxUserInfo.getAvatarUrl().isEmpty()) {
            user.setAvatar(wxUserInfo.getAvatarUrl());
            needUpdate = true;
        }
        if (wxUserInfo.getGender() != null) {
            user.setSex(wxUserInfo.getGender() == 1 ? 0 : (wxUserInfo.getGender() == 2 ? 1 : null));
            needUpdate = true;
        }

        if (needUpdate) {
            userService.updateUser(user);
        }
    }
}
