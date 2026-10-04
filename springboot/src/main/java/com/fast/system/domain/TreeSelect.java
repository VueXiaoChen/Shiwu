package com.fast.system.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 树结构实体类
 */
@Data
public class TreeSelect {
    //节点ID
    private Long id;

    //节点名称
    private String label;

    //子节点列表
    //为什么需要加上@JsonInclude注解?
    //控制JSON序列化时的行为
    //Include.NON_EMPTY 表示: 只有当children不为空时, 才包含这个字段到JSON中
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    /**
     * 构造函数: 将Menu对象转换为TreeSelect对象
     * 这个方法的核心作用就是进行数据转换和树形结构构建
     * @param menu 菜单对象
     *
     * 转换过程:
     * 1.复制基本信息: ID和名称
     * 2.递归转换子带单: 将子菜单也转换为TreeSelect对象
     *
     * 示例:
     * 输入: Menu对象 (menuId=1, menuName="系统管理"), 它有三个子菜单
     * 输出: TreeSelect对象(id=1,label="系统管理"), 它有三个TreeSelect子节点
     */
    public TreeSelect(Menu menu) {
        //1.设置当前节点的ID = 菜单的ID
        this.id = menu.getMenuId();

        //2.设置当前节点的显示名称 = 菜单的名称
        this.label = menu.getMenuName();

        //3.处理子菜单: 将Menu的子菜单列表转换为TreeSelect的子节点列表
        //整个过程相当于:
        // List<TreeSelect> childTreeSelects = new ArrayList<>();
        // for (Menu childMenu : menu.getChildren()) {
        //     TreeSelect childTreeSelect = new TreeSelect(childMenu);
        //     childTreeSelects.add(childTreeSelect);
        // }
        // this.children = childTreeSelects;

        this.children = menu.getChildren().stream()
                .map(TreeSelect::new)
                .collect(Collectors.toList());

        //注意: 这个地方使用了递归
        //每个TreeSelect对象创建时, 如果它有子菜单, 会继续创建子TreeSelect
        //直到没有子菜单的叶子节点为止
    }


}
