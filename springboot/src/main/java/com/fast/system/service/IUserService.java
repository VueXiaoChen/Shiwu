package com.fast.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.system.domain.User;

import java.util.List;

/**
 * 用户 service接口
 */
public interface IUserService {
    /**
     * 通过用户名查询用户
     * @param userName 用户名
     * @return 用户对象信息
     */
    public User selectUserByUserName(String userName);

    /**
     * 通过用户ID查询用户信息
     * @param userId 用户ID
     * @return 用户对象信息
     */
    User selectUserByUserId(Long userId);

    /**
     * 注册用户
     * @param newUser 要注册的用户信息
     * @return 是否注册成功
     */
    boolean registerUser(User newUser);

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatar 头像访问路径
     * @return 是否更新成功
     */
    int updateUserAvatar(Long userId, String avatar);

    /**
     * 修改用户信息
     * @param user 用户信息
     * @return 是否修改成功
     */
    int updateUser(User user);

    /**
     * 重置密码
     * @param userId 用户ID
     * @param newPassword 要修改成的密码
     * @return 是否重置成功
     */
    int resetUserPwd(Long userId, String newPassword);

    /**
     * 查询用户列表
     * @param user 查询参数
     * @return 用户列表数据
     */
    List<User> selectUserList(User user);

    /**
     * 分页查询用户列表（MyBatis Plus 分页）
     * @param page 分页对象
     * @param user 查询参数
     * @return 分页结果
     */
    Page<User> selectUserList(Page<User> page, User user);

    /**
     * 新增用户
     * @param user 表单参数
     * @return 是否新增成功
     */
    int insertUser(User user);

    /**
     * 删除用户
     * @param userIds 用户ID数组
     * @return 是否删除成功
     */
    int deleteUserByUserIds(Long[] userIds);

    /**
     * 通过微信 openId 查询用户
     * @param wxOpenId 微信小程序 openId
     * @return 用户对象信息，没找到返回 null
     */
    User selectUserByWxOpenId(String wxOpenId);
}
