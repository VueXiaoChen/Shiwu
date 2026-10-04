package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.RegisterBody;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户注册
 */
@RestController
public class RegisterController extends BaseController{
    @Resource
    private IUserService userService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody registerBody) {
        String userName = registerBody.getUserName();
        String password = registerBody.getPassword();

        //验证输入参数
        if (userName == null || userName.isEmpty()) {
            return error("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return error("密码不能为空");
        }

        //创建用户对象
        User newUser = new User();
        newUser.setUserName(userName);
        newUser.setPassword(password);

        //执行注册逻辑
        boolean regFlag = userService.registerUser(newUser);
        if (!regFlag) {
            return error("注册失败");
        }

        return success();
    }

}
