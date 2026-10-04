package com.fast.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.item.domain.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 失物/寻物信息 Mapper
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {
    /**
     * 分页查询物品列表（关联查询分类名称和发布人）
     * @param page 分页对象
     * @param item 查询参数
     * @return 分页结果
     */
    Page<Item> selectItemList(@Param("page") Page<Item> page, @Param("item") Item item);

    /**
     * 通过物品ID查询物品信息（关联查询分类名称和发布人）
     * @param itemId 物品ID
     * @return 物品对象信息
     */
    Item selectItemByItemId(Long itemId);
}
