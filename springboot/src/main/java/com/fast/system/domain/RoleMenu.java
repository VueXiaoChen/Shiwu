package com.fast.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单关联 role_menu
 */
@Data
@TableName("role_menu")
public class RoleMenu {
    //角色ID
    @TableField("role_id")
    private Long roleId;
    //菜单ID
    @TableField("menu_id")
    private Long menuId;
}
