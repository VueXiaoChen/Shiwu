package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.Menu;
import com.fast.system.domain.vo.RouterVo;
import com.fast.system.mapper.MenuMapper;
import com.fast.system.service.IMenuService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单信息
 */
@RestController
@RequestMapping("/system/menu")
public class MenuController extends BaseController {
    @Resource
    private IMenuService menuService;

    @Resource
    private MenuMapper menuMapper;

    /**
     * 查询菜单列表
     */
    @GetMapping("/selectMenuList")
    public AjaxResult selectMenuList(Menu menu) {
        List<Menu> list = menuService.selectMenuList(menu, SecurityUtils.getUserId());
        return success(list);
    }

    /**
     * 新增菜单
     */
    @PostMapping("/insertMenu")
    public AjaxResult insertMenu(@RequestBody Menu menu) {
        return toAjax(menuService.insertMenu(menu));
    }

    /**
     * 根据菜单ID查询菜单信息
     */
    @GetMapping("/selectMenuByMenuId/{menuId}")
    public AjaxResult selectMenuByMenuId(@PathVariable Long menuId) {
        return success(menuMapper.selectById(menuId));
    }

    /**
     * 修改菜单
     */
    @PutMapping("/updateMenu")
    public AjaxResult updateMenu(@RequestBody Menu menu) {
        //上级菜单不能选择自己, 例如: 将用户管理的上级菜单选择为用户管理
        if (menu.getMenuId().equals(menu.getParentId())) {
            return error("上级菜单不能选择自己");
        }

        return toAjax(menuService.updateMenu(menu));
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/deleteMenuByMenuId/{menuId}")
    public AjaxResult deleteMenuByMenuId(@PathVariable Long menuId) {
        return toAjax(menuMapper.deleteById(menuId));
    }

    /**
     * 根据角色ID查询对应菜单树
     */
    @GetMapping("/selectRoleMenuTree/{roleId}")
    public AjaxResult selectRoleMenuTree(@PathVariable Long roleId) {
        List<Menu> menus = menuService.selectMenuList(new Menu(), SecurityUtils.getUserId());
        AjaxResult ajax = AjaxResult.success();
        //根据角色ID查询菜单树信息
        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        //构建前端所需要的下拉树结构
        ajax.put("menus", menuService.buildMenuTreeSelect(menus));

        return ajax;
    }

    /**
     * 查询前端所需要的菜单下拉树结构
     */
    @GetMapping("/selectRoleMenusTree")
    public AjaxResult selectRoleMenusTree() {
        List<Menu> menus = menuService.selectMenuList(new Menu(), SecurityUtils.getUserId());
        //构建前端所需要的下拉树结构
        return success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 获取路由信息(菜单权限)
     * 功能: 获取当前用户的菜单权限, 用于前端动态生成路由
     */
    @GetMapping("/getRouters")
    public AjaxResult getRouters() {
        //获取当前用户的ID
        Long userId = SecurityUtils.getUserId();
        //根据用户ID查询该用户的菜单树并且构建成前端需要的路由格式
        List<RouterVo> routers = menuService.selectMenuTreeRouterByUserId(userId);
        return success(routers);
    }

}
