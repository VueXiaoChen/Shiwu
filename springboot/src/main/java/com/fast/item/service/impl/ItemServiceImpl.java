package com.fast.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
import com.fast.item.service.IItemService;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 失物/寻物信息 业务处理类
 */
@Service
public class ItemServiceImpl implements IItemService {
    @Resource
    private ItemMapper itemMapper;

    /**
     * 分页查询物品列表
     * @param page 分页对象
     * @param item 查询参数
     * @return 分页结果
     */
    @Override
    public Page<Item> selectItemList(Page<Item> page, Item item) {
        //列表要展示分类名称和发布人, 所以走XML里的联表查询
        return itemMapper.selectItemList(page, item);
    }

    /**
     * 发布物品信息(小程序端)
     * @param item 表单参数
     * @return 是否新增成功
     */
    @Override
    public int insertItem(Item item) {
        //发布人不信前端传的, 直接取当前登录用户, 防止冒充别人发布
        item.setUserId(SecurityUtils.getUserId());
        //新发布的信息默认是"进行中", 浏览量从0开始
        item.setStatus("open");
        item.setViews(0);
        return itemMapper.insert(item);
    }

    /**
     * 修改物品信息
     * @param item 表单参数
     * @return 是否修改成功
     */
    @Override
    public int updateItem(Item item) {
        return itemMapper.updateById(item);
    }

    /**
     * 标记物品为已完成(小程序端)
     * @param itemId 物品ID
     * @return 是否修改成功
     */
    @Override
    public int finishItem(Long itemId) {
        //先把这条信息查出来, 确认是自己发的才能标记
        checkOwner(itemId);
        return itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getItemId, itemId)
                .set(Item::getStatus, "done"));
    }

    /**
     * 删除自己发布的物品(小程序端)
     * @param itemId 物品ID
     * @return 是否删除成功
     */
    @Override
    public int deleteMyItem(Long itemId) {
        //只能删自己发的, 别人的删不了
        checkOwner(itemId);
        return itemMapper.deleteById(itemId);
    }

    /**
     * 浏览量+1(详情页打开时调用)
     * @param itemId 物品ID
     * @return 是否修改成功
     */
    @Override
    public int increaseViews(Long itemId) {
        //直接在数据库里给views加1, 不用先查再改
        return itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getItemId, itemId)
                .setSql("views = views + 1"));
    }

    /**
     * 删除物品信息
     * @param itemIds 物品ID数组
     * @return 是否删除成功
     */
    @Override
    public int deleteItemByItemIds(Long[] itemIds) {
        return itemMapper.deleteBatchIds(Arrays.asList(itemIds));
    }

    /**
     * 校验物品是不是当前登录用户发布的
     * 不是自己的就直接抛异常拦下来
     * @param itemId 物品ID
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
