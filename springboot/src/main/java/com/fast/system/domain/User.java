package com.fast.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户对象 user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User {
    //用户ID
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    //用户名
    @TableField("user_name")
    private String userName;

    //用户性别
    private Integer sex;

    //用户头像
    private String avatar;

    //密码
    private String password;

    //创建时间
    // @JsonFormat 可以控制JSON格式转换
    // pattern = "yyyy-MM-dd HH:mm:ss" 表示格式化为: xxxx-xx-xx xx:xx:xx
    // 作用: 当这个对象转为json返回给我们的前端时, 日期会按这个格式进行显示
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;

    //角色ID
    @TableField("role_id")
    private Long roleId;

    //角色名称 (该字段用于关联查询，不存在于user表中)
    @TableField(exist = false)
    private String roleName;

    //微信小程序 openId（用于微信一键登录，关联微信用户身份）
    @TableField("wx_openid")
    private String wxOpenId;

}
