package com.fast.item.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 失物/寻物信息对象 item
 */
@Data
@TableName("item")
public class Item {
    //物品ID
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;
    //信息类型(lost寻物启事 found失物招领)
    @TableField("type")
    private String type;
    //标题
    @TableField("title")
    private String title;
    //详细描述
    @TableField("description")
    private String description;
    //分类ID
    @TableField("category_id")
    private Long categoryId;
    //图片(多张用逗号分隔)
    @TableField("images")
    private String images;
    //丢失/拾取地点
    @TableField("location")
    private String location;
    //丢失/拾取日期(只精确到天, 加时区避免差一天)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("happen_time")
    private Date happenTime;
    //联系方式
    @TableField("contact")
    private String contact;
    //是否急寻(0否 1是)
    @TableField("urgent")
    private Integer urgent;
    //悬赏说明
    @TableField("reward")
    private String reward;
    //状态(open进行中 done已完成)
    @TableField("status")
    private String status;
    //浏览量
    @TableField("views")
    private Integer views;
    //发布人用户ID
    @TableField("user_id")
    private Long userId;
    //发布时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private Date createTime;

    //分类名称(关联查询用, 不是item表的字段)
    @TableField(exist = false)
    private String categoryName;
    //发布人用户名(关联查询用, 不是item表的字段)
    @TableField(exist = false)
    private String userName;
    //发布人头像(关联查询用, 不是item表的字段)
    @TableField(exist = false)
    private String userAvatar;
    //搜索关键词(小程序搜索用, 一个词同时匹配标题/描述/地点)
    @TableField(exist = false)
    private String keyword;

    private String personTags;    // 人物特征标签，如 "美女,长发"
    private String faceFeature;   // 人脸特征向量，JSON 数组字符串
    private String itemFeature;   // 物品图片特征向量，JSON 数组字符串
}
