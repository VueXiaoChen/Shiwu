package com.fast.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏统计 Mapper（管理端首页用, 只做聚合查询不做增删改）
 */
@Mapper
public interface DashboardMapper {
    /**
     * 查询近7天每天的寻物/招领发布数量
     * @return 每天一行: day(日期字符串)、lost(寻物数)、found(招领数)
     */
    List<Map<String, Object>> selectTrend7d();

    /**
     * 查询各分类下的物品数量分布
     * @return 每个分类一行: name(分类名称)、value(物品数量)
     */
    List<Map<String, Object>> selectCategoryDist();
}
