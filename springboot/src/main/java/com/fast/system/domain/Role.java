package com.fast.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 角色对象 role
 */
@Data
@TableName("role")
public class Role {
    //角色ID
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;
    //角色名称
    @TableField("role_name")
    private String roleName;
    //显示顺序
    @TableField("role_sort")
    private Integer roleSort;
    //备注
    @TableField("remark")
    private String remark;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;
    //菜单组
    @TableField(exist = false)
    private Long[] menuIds;
}
