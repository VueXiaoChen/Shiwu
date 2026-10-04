package com.fast.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.system.domain.User;
import com.fast.system.mapper.UserMapper;
import com.fast.system.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 用户 service实现方法
 */
@Service
public class UserServiceImpl implements IUserService {
    @Resource
    private UserMapper userMapper;

    /**
     * 通过用户名查询用户
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public User selectUserByUserName(String userName) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserName, userName));
    }

    /**
     * 通过用户ID查询用户信息
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public User selectUserByUserId(Long userId) {
        return userMapper.selectUserByUserId(userId);
    }

    /**
     * 注册用户
     * @param newUser 要注册的用户信息
     * @return 是否注册成功
     */
    @Override
    public boolean registerUser(User newUser) {
        //根据用户名查询用户信息
        User user = selectUserByUserName(newUser.getUserName());

        if (user != null) {
            throw new RuntimeException("用户名已存在, 请更换用户名后再注册");
        }

        // MyBatis Plus 的 insert 方法，自动回填主键
        int i = userMapper.insert(newUser);
        return i > 0;
    }

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatar 头像访问路径
     * @return 是否更新成功
     */
    @Override
    public int updateUserAvatar(Long userId, String avatar) {
        return userMapper.update(null,
                new LambdaUpdateWrapper<User>().set(User::getAvatar, avatar).eq(User::getUserId, userId));
    }

    /**
     * 修改用户信息
     * @param user 用户信息
     * @return 是否修改成功
     */
    @Override
    @Transactional
    public int updateUser(User user) {
        //修改用户信息（MyBatis Plus 动态更新，含 role_id）
        return userMapper.updateById(user);
    }

    /**
     * 重置密码
     * @param userId 用户ID
     * @param newPassword 要修改成的密码
     * @return 是否重置成功
     */
    @Override
    public int resetUserPwd(Long userId, String newPassword) {
        return userMapper.update(null,
                new LambdaUpdateWrapper<User>().set(User::getPassword, newPassword).eq(User::getUserId, userId));
    }

    /**
     * 查询用户列表
     * @param user 查询参数
     * @return 用户列表数据
     */
    @Override
    public List<User> selectUserList(User user) {
        return userMapper.selectUserList(user);
    }

    /**
     * 分页查询用户列表
     * @param page 分页对象
     * @param user 查询参数
     * @return 分页结果
     */
    @Override
    public Page<User> selectUserList(Page<User> page, User user) {
        return userMapper.selectUserList(page, user);
    }

    /**
     * 新增用户
     * @param user 表单参数
     * @return 是否新增成功
     */
    @Override
    @Transactional
    public int insertUser(User user) {
        //新增用户（MyBatis Plus 插入，自动回填主键）
        return userMapper.insert(user);
    }

    /**
     * 删除用户
     * @param userIds 用户ID数组
     * @return 是否删除成功
     */
    @Override
    public int deleteUserByUserIds(Long[] userIds) {
        // 外键 ON DELETE CASCADE 自动清理 user_role 关联记录
        return userMapper.deleteBatchIds(Arrays.asList(userIds));
    }

    /**
     * 通过微信 openId 查询用户
     * @param wxOpenId 微信小程序 openId
     * @return 用户对象信息，没找到返回 null
     */
    @Override
    public User selectUserByWxOpenId(String wxOpenId) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getWxOpenId, wxOpenId)
        );
    }

}
