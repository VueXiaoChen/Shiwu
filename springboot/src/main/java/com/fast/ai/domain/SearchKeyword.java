package com.fast.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("search_keyword")
public class SearchKeyword {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private Integer enabled;
    private Integer sortOrder;
    private Date createTime;
}