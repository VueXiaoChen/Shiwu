package com.fast.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.system.domain.Role;

import java.util.List;

/**
 * 角色 Service接口
 */
public interface IRoleService {

    /**
     * 查询角色列表
     * @return 角色列表数据
     */
    List<Role> selectRoleList(Role role);

    /**
     * 分页查询角色列表（MyBatis Plus 分页）
     * @param page 分页对象
     * @param role 查询参数
     * @return 分页结果
     */
    Page<Role> selectRoleList(Page<Role> page, Role role);

    /**
     * 新增角色
     * @param role 表单参数
     * @return 是否新增成功
     */
    int insertRole(Role role);

    /**
     * 修改角色
     * @param role 表单参数
     * @return 是否修改成功
     */
    int updateRole(Role role);

    /**
     * 删除角色
     * @param roleIds 角色ID数组
     * @return 是否删除成功
     */
    int deleteRoleByRoleIds(Long[] roleIds);
}
