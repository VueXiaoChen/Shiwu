package com.fast.item.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.item.domain.Item;

/**
 * 失物/寻物信息 业务接口
 */
public interface IItemService {
    /**
     * 分页查询物品列表
     * @param page 分页对象
     * @param item 查询参数
     * @return 分页结果
     */
    Page<Item> selectItemList(Page<Item> page, Item item);

    /**
     * 发布物品信息(小程序端, 发布人取当前登录用户)
     * @param item 表单参数
     * @return 是否新增成功
     */
    int insertItem(Item item);

    /**
     * 修改物品信息
     * @param item 表单参数
     * @return 是否修改成功
     */
    int updateItem(Item item);

    /**
     * 标记物品为已完成(小程序端, 只能操作自己发布的)
     * @param itemId 物品ID
     * @return 是否修改成功
     */
    int finishItem(Long itemId);

    /**
     * 删除自己发布的物品(小程序端, 只能删自己的)
     * @param itemId 物品ID
     * @return 是否删除成功
     */
    int deleteMyItem(Long itemId);

    /**
     * 浏览量+1(详情页打开时调用)
     * @param itemId 物品ID
     * @return 是否修改成功
     */
    int increaseViews(Long itemId);

    /**
     * 删除物品信息
     * @param itemIds 物品ID数组
     * @return 是否删除成功
     */
    int deleteItemByItemIds(Long[] itemIds);
}
