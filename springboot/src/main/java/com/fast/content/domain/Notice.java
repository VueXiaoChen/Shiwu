package com.fast.content.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 公告对象 notice
 */
@Data
@TableName("notice")
public class Notice {
    //公告ID
    @TableId(value = "notice_id", type = IdType.AUTO)
    private Long noticeId;
    //公告标题
    @TableField("title")
    private String title;
    //公告内容(富文本HTML)
    @TableField("content")
    private String content;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;
}
