package com.fast.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.system.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 通过用户ID查询用户信息（关联查询角色）
     * @param userId 用户ID
     * @return 用户对象信息
     */
    User selectUserByUserId(Long userId);

    /**
     * 查询用户列表（关联查询角色）
     * @param user 查询参数
     * @return 用户列表数据
     */
    List<User> selectUserList(@Param("user") User user);

    /**
     * 分页查询用户列表（MyBatis Plus 分页）
     * @param page 分页对象
     * @param user 查询参数
     * @return 分页结果
     */
    Page<User> selectUserList(@Param("page") Page<User> page, @Param("user") User user);
}
