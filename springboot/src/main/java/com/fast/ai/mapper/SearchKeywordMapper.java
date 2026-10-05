package com.fast.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fast.ai.domain.SearchKeyword;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SearchKeywordMapper extends BaseMapper<SearchKeyword> {
}