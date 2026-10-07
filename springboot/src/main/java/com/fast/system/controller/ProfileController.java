package com.fast.system.controller;

import com.fast.system.config.fastConfig;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.LoginUser;
import com.fast.system.domain.SubmitPwdBody;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/system/user/profile")
public class ProfileController extends BaseController {

    @Resource
    private IUserService userService;

    @Resource
    private fastConfig fastConfig;

    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            LoginUser loginUser = SecurityUtils.getLoginUser();

            // 统一到 {profile}/file/avatar
            String uploadDir = fastConfig.getProfile() + "/file/avatar";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String uniqueFilename = uuid + extension;

            Path filePath = Paths.get(uploadDir, uniqueFilename);
            Files.write(filePath, file.getBytes());

            String avatar = "/file/avatar/" + uniqueFilename;

            if (userService.updateUserAvatar(loginUser.getUserId(), avatar) > 0) {
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                loginUser.getUser().setAvatar(avatar);
                return ajax;
            }
        }
        return error("上传头像失败, 请重新上传");
    }

    @PutMapping
    public AjaxResult updateProfile(@RequestBody User user) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        User currentUser = loginUser.getUser();
        currentUser.setUserName(user.getUserName());
        currentUser.setSex(user.getSex());
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            currentUser.setAvatar(user.getAvatar());
        }
        return toAjax(userService.updateUser(currentUser));
    }

    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody SubmitPwdBody submitPwdBody) {
        String oldPassword = submitPwdBody.getOldPassword();
        String newPassword = submitPwdBody.getNewPassword();

        LoginUser loginUser = SecurityUtils.getLoginUser();
        User user = loginUser.getUser();

        String password = user.getPassword();
        if (newPassword.equals(password)) {
            return error("新密码不能与旧密码相同");
        }
        if (!oldPassword.equals(password)) {
            return error("旧密码错误");
        }

        if (userService.resetUserPwd(user.getUserId(), newPassword) > 0) {
            loginUser.getUser().setPassword(newPassword);
            return success();
        }

        return error("修改密码失败, 请重新填写后提交");
    }
}