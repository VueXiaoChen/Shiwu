package com.fast.system.controller;

import com.fast.system.configure.TokenService;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginBody;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录验证
 */
@RestController
public class LoginController extends BaseController {
    @Resource
    private IUserService userService;

    @Resource
    private TokenService tokenService;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        //步骤1: 验证参数是否为空
        if (loginBody.getUserName() == null || loginBody.getPassword() == null
            || loginBody.getUserName().trim().isEmpty() || loginBody.getPassword().trim().isEmpty()) {
            //抛出异常
            throw new RuntimeException("用户名或密码为空");
        }

        //步骤2: 验证用户是否存在
        User user = userService.selectUserByUserName(loginBody.getUserName());
        if (user == null) {
            throw new RuntimeException("用户名错误");
        }

        //步骤3: 验证密码是否正确
        if (!loginBody.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        //步骤4: 创建登录用户对象
        LoginUser loginUser = new LoginUser(user.getUserId(), user);

        //步骤5: 生成JWT令牌
        String token = tokenService.createToken(loginUser);

        //步骤6: 返回成功结果, 并且包含token
        return success().put("token", token);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/getInfo")
    public AjaxResult getInfo() {
        //步骤1: 获取当前登录用户的用户ID
        Long userId = SecurityUtils.getUserId();
        //步骤2: 根据用户ID查询用户信息
        User user = userService.selectUserByUserId(userId);
        //步骤3: 返回用户信息
        return success(user);
    }

}
