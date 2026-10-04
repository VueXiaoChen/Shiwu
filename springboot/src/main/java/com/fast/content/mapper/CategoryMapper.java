package com.fast.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fast.content.domain.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
