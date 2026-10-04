package com.fast.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 菜单 menu
 */
@Data
@TableName("menu")
public class Menu {
    //菜单ID
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    //菜单名称
    @TableField("menu_name")
    private String menuName;

    //父菜单ID
    @TableField("parent_id")
    private Long parentId;

    //菜单排序
    @TableField("menu_sort")
    private Integer menuSort;

    //路由地址
    private String path;

    //组件路径
    private String component;

    //菜单类型(M目录 C菜单)
    @TableField("menu_type")
    private String menuType;

    //菜单图标
    private String icon;

    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;

    //子菜单
    @TableField(exist = false)
    private List<Menu> children = new ArrayList<Menu>();

    //用户ID
    @TableField(exist = false)
    private Long userId;
}
