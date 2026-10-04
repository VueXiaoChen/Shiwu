package com.fast.system.service;

import com.fast.system.domain.Menu;
import com.fast.system.domain.TreeSelect;
import com.fast.system.domain.vo.RouterVo;

import java.util.List;

/**
 * 菜单 service
 */
public interface IMenuService {

    /**
     * 查询菜单列表
     * @param menu 查询参数
     * @return 菜单列表数据
     */
    List<Menu> selectMenuList(Menu menu, Long userId);

    /**
     * 新增菜单
     * @param menu 表单参数
     * @return 是否新增成功
     */
    int insertMenu(Menu menu);

    /**
     * 修改菜单
     * @param menu 表单参数
     * @return 是否修改成功
     */
    int updateMenu(Menu menu);

    /**
     * 根据角色ID查询对应菜单树
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    List<Long> selectMenuListByRoleId(Long roleId);

    /**
     * 构建前端所需要的下拉树结构
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    List<TreeSelect> buildMenuTreeSelect(List<Menu> menus);

    /**
     * 根据用户ID查询该用户的菜单树并且构建成前端需要的路由格式
     * @param userId 用户ID
     * @return 路由列表
     */
    List<RouterVo> selectMenuTreeRouterByUserId(Long userId);
}
