package com.fast.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fast.system.domain.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据用户ID查询菜单列表
     * @param menu 查询参数
     * @return 菜单列表数据
     */
    List<Menu> selectMenuListByUserId(Menu menu);

    /**
     * 根据角色ID查询对应菜单树
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    List<Long> selectMenuListByRoleId(Long roleId);
}
