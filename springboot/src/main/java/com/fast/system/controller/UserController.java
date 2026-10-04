package com.fast.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.domain.User;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息
 */
@RestController
@RequestMapping("/system/user")
public class UserController extends BaseController{
    @Resource
    private IUserService userService;

    /**
     * 查询用户列表
     */
    @GetMapping("/selectUserList")
    public TableDataInfo selectUserList(User user) {
        // 启动 MyBatis Plus 分页
        Page<User> page = startPage();
        // 分页查询用户列表
        page = userService.selectUserList(page, user);
        return getDataTable(page);
    }

    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/selectUserByUserId/{userId}")
    public AjaxResult selectUserByUserId(@PathVariable Long userId) {
        User user = userService.selectUserByUserId(userId);
        return success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping("/insertUser")
    public AjaxResult insertUser(@RequestBody User user) {
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PutMapping("/updateUser")
    public AjaxResult updateUser(@RequestBody User user) {
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/deleteUserByUserIds/{userIds}")
    public AjaxResult deleteUserByUserIds(@PathVariable Long[] userIds) {
        return toAjax(userService.deleteUserByUserIds(userIds));
    }

}
