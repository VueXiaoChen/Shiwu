package com.fast.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fast.system.domain.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

/**
 * 角色与菜单关联 数据层
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 批量新增角色菜单关联
     * @param roleMenuList 角色菜单列表
     * @return 是否新增成功
     */
    int batchRoleMenu(ArrayList<RoleMenu> roleMenuList);
}
