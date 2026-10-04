package com.fast.system.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路由显示信息
 * 用于存储前端页面导航项的显示配置
 */
@Data
@NoArgsConstructor
public class MetaVo {
    //导航菜单项显示的标题文字
    private String title;

    //导航菜单项对应的图标
    private String icon;

    //路由访问地址
    private String path;

    //目录构造函数 - 包含标题和图标
    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    //菜单构造函数 - 包含标题和图标和路由访问地址
    public MetaVo(String title, String icon, String path) {
        this.title = title;
        this.icon = icon;
        this.path = path;
    }

}
