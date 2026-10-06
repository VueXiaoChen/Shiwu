package com.fast.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.ai.service.FeatureExtractService;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
import com.fast.item.service.IItemService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 失物/寻物信息 业务处理类
 */
@Slf4j
@Service
public class ItemServiceImpl implements IItemService {

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private FeatureExtractService featureExtractService;

    /**
     * 分页查询物品列表
     */
    @Override
    public Page<Item> selectItemList(Page<Item> page, Item item) {
        return itemMapper.selectItemList(page, item);
    }

    /**
     * 发布物品信息(小程序端)
     */
    @Override
    public int insertItem(Item item) {
        // ★ 兜底：如果没传 userId，用当前登录用户补上
        if (item.getUserId() == null) {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                throw new RuntimeException("请先登录后再发布");
            }
            item.setUserId(userId);
        }

        int rows = itemMapper.insert(item);
        if (rows > 0 && item.getImages() != null && !item.getImages().isBlank()) {
            try {
                featureExtractService.extractAndSave(item.getItemId(), item.getImages());
            } catch (Exception e) {
                log.warn("发布后特征提取失败 itemId={}", item.getItemId(), e);
            }
        }
        return rows;
    }

    /**
     * 修改物品信息
     */
    @Override
    public int updateItem(Item item) {
        int rows = itemMapper.updateById(item);
        if (rows > 0 && item.getImages() != null && !item.getImages().isBlank()) {
            try {
                featureExtractService.extractAndSave(item.getItemId(), item.getImages());
            } catch (Exception e) {
                log.warn("更新后特征提取失败 itemId={}", item.getItemId(), e);
            }
        }
        return rows;
    }

    /**
     * 标记物品为已完成(小程序端)
     */
    @Override
    public int finishItem(Long itemId) {
        checkOwner(itemId);
        return itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getItemId, itemId)
                .set(Item::getStatus, "done"));
    }

    /**
     * 删除自己发布的物品(小程序端)
     */
    @Override
    public int deleteMyItem(Long itemId) {
        checkOwner(itemId);
        return itemMapper.deleteById(itemId);
    }

    /**
     * 浏览量+1(详情页打开时调用)
     */
    @Override
    public int increaseViews(Long itemId) {
        return itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getItemId, itemId)
                .setSql("views = views + 1"));
    }

    /**
     * 删除物品信息
     */
    @Override
    public int deleteItemByItemIds(Long[] itemIds) {
        return itemMapper.deleteBatchIds(Arrays.asList(itemIds));
    }

    /**
     * 校验物品是不是当前登录用户发布的
     */
    private void checkOwner(Long itemId) {
        Item dbItem = itemMapper.selectById(itemId);
        if (dbItem == null) {
            throw new RuntimeException("信息不存在或已被删除");
        }
        if (!dbItem.getUserId().equals(SecurityUtils.getUserId())) {
            throw new RuntimeException("只能操作自己发布的信息");
        }
    }
}