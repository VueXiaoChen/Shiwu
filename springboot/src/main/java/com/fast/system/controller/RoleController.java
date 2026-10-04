package com.fast.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.Role;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.mapper.RoleMapper;
import com.fast.system.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 角色信息
 */
@RestController
@RequestMapping("/system/role")
public class RoleController extends BaseController{
    @Resource
    private IRoleService roleService;

    @Resource
    private RoleMapper roleMapper;

    /**
     * 查询所有角色列表
     */
    @GetMapping("/selectAllRole")
    public AjaxResult selectAllRole() {
        return success(roleService.selectRoleList(new Role()));
    }

    /**
     * 查询角色列表
     */
    @GetMapping("/selectRoleList")
    public TableDataInfo selectRoleList(Role role) {
        Page<Role> page = startPage();
        page = roleService.selectRoleList(page, role);
        return getDataTable(page);
    }

    /**
     * 根据角色ID查询角色信息
     */
    @GetMapping("/selectRoleByRoleId/{roleId}")
    public AjaxResult selectRoleByRoleId(@PathVariable Long roleId) {
        return success(roleMapper.selectById(roleId));
    }

    /**
     * 新增角色
     */
    @PostMapping("/insertRole")
    public AjaxResult insertRole(@RequestBody Role role) {
        return toAjax(roleService.insertRole(role));
    }

    /**
     * 修改角色
     */
    @PutMapping("/updateRole")
    public AjaxResult updateRole(@RequestBody Role role) {
        return toAjax(roleService.updateRole(role));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/deleteRoleByRoleIds/{roleIds}")
    public AjaxResult deleteRoleByRoleIds(@PathVariable Long[] roleIds) {
        for (Long roleId : roleIds) {
            if (roleId == 1L || roleId == 2L) {
                return error("超级管理员和普通用户是系统的核心角色, 不允许删除");
            }
        }
        return toAjax(roleService.deleteRoleByRoleIds(roleIds));
    }

}
