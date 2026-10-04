package com.fast.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.system.domain.Role;
import com.fast.system.domain.RoleMenu;
import com.fast.system.mapper.RoleMapper;
import com.fast.system.mapper.RoleMenuMapper;
import com.fast.system.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 角色 业务处理类
 */
@Service
public class RoleServiceImpl implements IRoleService {
    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    /**
     * 查询角色列表
     * @return 角色列表数据
     */
    @Override
    public List<Role> selectRoleList(Role role) {
        return roleMapper.selectRoleList(role);
    }

    /**
     * 分页查询角色列表
     * @param page 分页对象
     * @param role 查询参数
     * @return 分页结果
     */
    @Override
    public Page<Role> selectRoleList(Page<Role> page, Role role) {
        return roleMapper.selectRoleList(page, role);
    }

    /**
     * 新增角色
     * @param role 表单参数
     * @return 是否新增成功
     */
    @Override
    @Transactional
    public int insertRole(Role role) {
        //新增角色（MyBatis Plus 插入，自动回填主键）
        roleMapper.insert(role);
        //新增角色菜单关联信息
        return insetRoleMenu(role);
    }

    /**
     * 修改角色
     * @param role 表单参数
     * @return 是否修改成功
     */
    @Override
    @Transactional
    public int updateRole(Role role) {
        //修改角色（MyBatis Plus 动态更新）
        roleMapper.updateById(role);
        //根据角色ID删除角色和菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, role.getRoleId()));
        //新增角色菜单信息
        return insetRoleMenu(role);
    }

    /**
     * 新增角色菜单信息
     */
    public int insetRoleMenu(Role role) {
        int rows = 1;
        //新增用户与角色关联的数据
        ArrayList<RoleMenu> list = new ArrayList<>();
        for (Long menuId : role.getMenuIds()) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0) {
            //批量新增角色菜单关联
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 删除角色
     * @param roleIds 角色ID数组
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public int deleteRoleByRoleIds(Long[] roleIds) {
        // 外键 ON DELETE CASCADE 自动清理 role_menu 关联记录
        return roleMapper.deleteBatchIds(Arrays.asList(roleIds));
    }
}
