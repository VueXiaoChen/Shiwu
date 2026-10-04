package com.fast.content.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 物品分类对象 category
 */
@Data
@TableName("category")
public class Category {
    //分类ID
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;
    //分类名称
    @TableField("category_name")
    private String categoryName;
    //分类图标
    @TableField("icon")
    private String icon;
    //显示排序
    @TableField("category_sort")
    private Integer categorySort;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;
}
