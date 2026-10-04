package com.fast.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.system.domain.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 查询角色列表
     * @return 角色列表数据
     */
    List<Role> selectRoleList(@Param("role") Role role);

    /**
     * 分页查询角色列表（MyBatis Plus 分页）
     * @param page 分页对象
     * @param role 查询参数
     * @return 分页结果
     */
    Page<Role> selectRoleList(@Param("page") Page<Role> page, @Param("role") Role role);
}
