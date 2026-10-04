package com.fast.message.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户留言消息对象 message
 * 一条记录就是一个用户针对某件物品发给另一个用户的一句留言
 */
@Data
@TableName("message")
public class Message {
    //消息ID
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;
    //关联物品ID
    @TableField("item_id")
    private Long itemId;
    //发送人用户ID
    @TableField("from_user_id")
    private Long fromUserId;
    //接收人用户ID
    @TableField("to_user_id")
    private Long toUserId;
    //消息内容
    @TableField("content")
    private String content;
    //是否已读(0未读 1已读)
    @TableField("is_read")
    private Integer isRead;
    //发送时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private Date createTime;

    //发送人用户名(关联查询用, 不是message表的字段)
    @TableField(exist = false)
    private String fromUserName;
    //发送人头像(关联查询用, 不是message表的字段)
    @TableField(exist = false)
    private String fromUserAvatar;
    //接收人用户名(关联查询用, 不是message表的字段)
    @TableField(exist = false)
    private String toUserName;
    //接收人头像(关联查询用, 不是message表的字段)
    @TableField(exist = false)
    private String toUserAvatar;
    //关联物品标题(关联查询用, 不是message表的字段)
    @TableField(exist = false)
    private String itemTitle;
    //会话未读数(小程序会话列表统计用, 不是message表的字段)
    @TableField(exist = false)
    private Integer unreadCount;
    //会话留言总数(管理端会话列表统计用, 不是message表的字段)
    @TableField(exist = false)
    private Integer messageCount;
}
