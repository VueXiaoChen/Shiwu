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

/**
 * 个人信息
 */
@RestController
@RequestMapping("/system/user/profile")
public class ProfileController extends BaseController {
    @Resource
    private IUserService userService;

    /**
     * 头像上传
     */
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam MultipartFile file) throws IOException {
        //第一步: 检查用户是否上传了文件
        //这就像快递员先检查包裹里有没有东西
        //如果用户点击了上传但是没有选择文件, file就是空的
        if (!file.isEmpty()) {
            //第二步: 获取当前登录用户的信息
            //这就像快递员要问"这个包裹是谁寄的"
            LoginUser loginUser = SecurityUtils.getLoginUser();

            //第三步: 准备存放头像的文件夹
            //fastConfig.getProfile(): 从配置文件中获取基础的存储路径 比如./file
            //再加上/avatar: 在基础路径下创建avatar文件夹用来专门存放头像
            //完整路径: ./file/avatar
            String uploadDir = fastConfig.getProfile() + "/avatar";

            //创建一个File对象, 代表这个目录路径
            File dir = new File(uploadDir);

            //看看这个文件夹是否存在
            if (!dir.exists()) {
                //!dir.exists(): 如果文件夹不存在, 就创建它
                //mkdirs() 会创建所有不存在的父目录
                //想象一下: 快递站发现没有存放包裹的货架, 就立即搭建一个
                dir.mkdirs();
            }

            //第四步:为上传的文件生成一个唯一的文件名
            //file.getOriginalFilename(); 获取用户上传的文件的原始文件名
            //比如用户上传的是"aaa.jpg"
            String originalFilename = file.getOriginalFilename();

            //获取文件的扩展名
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                //如果文件名没有拓展名, 就赋值为空字符串
                extension = "";
            }

            //UUID.randomUUID() 生成一个uuid
            //.replaceAll("-", "") 去掉所有横线
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");

            //最终文件名 = uuid + 拓展名
            //例如: 545as45ads45asd4a5sd4ads5.jpg
            String uniqueFilename = uuid + extension;

            //第五步: 将文件保存到服务器的硬盘上
            //Paths.get(uploadDir, uniqueFilename); 构建完整的文件保存路径
            Path filePath = Paths.get(uploadDir, uniqueFilename);

            //Files.write 将文件内容写入到指定路径
            //file.getBytes() 获取上传文件的字节数据
            Files.write(filePath, file.getBytes());

            //第六步: 构建头像的访问路径(给前端用的地址)
            String avatar = "/profile/avatar/" + uniqueFilename;

            //第七步: 更新数据库中的用户头像信息
            if (userService.updateUserAvatar(loginUser.getUserId(), avatar) > 0) {
                //第八步: 准备返回给前端的成功响应
                AjaxResult ajax = AjaxResult.success();

                //ajax.put("imgUrl", avatar); 在响应数据中添加头像地址
                //前端可以通过data.imgUrl获取到这个地址
                ajax.put("imgUrl", avatar);

                //第九步: 更新缓存中的用户头像信息
                loginUser.getUser().setAvatar(avatar);

                //第十步: 返回成功响应给前端
                return ajax;
            }
        }

        //第十一步: 如果上传失败, 返回错误信息
        //有两种情况会走到这里:
        //1.用户没有选择文件(没有上传文件)
        //2.文件保存成功, 但是数据库更新失败
        return error("上传头像失败, 请重新上传");
    }

    /**
     * 修改个人信息
     */
    @PutMapping
    public AjaxResult updateProfile(@RequestBody User user) {
        //获取当前用户数据
        LoginUser loginUser = SecurityUtils.getLoginUser();

        //提取用户信息
        User currentUser = loginUser.getUser();

        //设置要更新的用户信息
        currentUser.setUserName(user.getUserName());
        currentUser.setSex(user.getSex());
        // 微信小程序设置头像时传的是头像URL，一并保存
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            currentUser.setAvatar(user.getAvatar());
        }

        //调用服务层更新用户信息
        return toAjax(userService.updateUser(currentUser));
    }

    /**
     * 重置密码
     */
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody SubmitPwdBody submitPwdBody) {
        //用户输入的老密码
        String oldPassword = submitPwdBody.getOldPassword();
        //用户输入的新密码
        String newPassword = submitPwdBody.getNewPassword();

        //当前用户信息
        LoginUser loginUser = SecurityUtils.getLoginUser();
        User user = loginUser.getUser();

        //用户密码修改前的密码
        String password = user.getPassword();
        if (newPassword.equals(password)) {
            return error("新密码不能与旧密码相同");
        }
        if (!oldPassword.equals(password)) {
            return error("旧密码错误");
        }

        //修改该用户的密码
        if (userService.resetUserPwd(user.getUserId(), newPassword) > 0) {
            //更新缓存用户密码
            loginUser.getUser().setPassword(newPassword);
            return success();
        }

        return error("修改密码失败, 请重新填写后提交");
    }

}
