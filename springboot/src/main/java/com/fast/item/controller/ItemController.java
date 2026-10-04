package com.fast.item.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
import com.fast.item.service.IItemService;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import com.fast.system.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 失物/寻物信息（管理端 + 小程序端）
 */
@RestController
@RequestMapping("/item/item")
public class ItemController extends BaseController {
    @Resource
    private IItemService itemService;

    @Resource
    private ItemMapper itemMapper;

    /**
     * 查询物品列表
     * 管理端列表和小程序首页/搜索共用, 支持 title/type/categoryId/status/urgent/keyword 筛选
     */
    @GetMapping("/selectItemList")
    public TableDataInfo selectItemList(Item item) {
        Page<Item> page = startPage();
        page = itemService.selectItemList(page, item);
        return getDataTable(page);
    }

    /**
     * 查询我发布的物品列表(小程序"我的发布"页用)
     */
    @GetMapping("/selectMyItemList")
    public TableDataInfo selectMyItemList(Item item) {
        //发布人固定为当前登录用户, 只能看到自己发的
        item.setUserId(SecurityUtils.getUserId());
        Page<Item> page = startPage();
        page = itemService.selectItemList(page, item);
        return getDataTable(page);
    }

    /**
     * 首页统计数字(全部/招领/寻物的条数)
     */
    @GetMapping("/selectItemStats")
    public AjaxResult selectItemStats() {
        Long total = itemMapper.selectCount(null);
        Long found = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getType, "found"));
        Long lost = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getType, "lost"));
        AjaxResult ajax = success();
        ajax.put("total", total);
        ajax.put("found", found);
        ajax.put("lost", lost);
        return ajax;
    }

    /**
     * 根据物品ID查询物品信息
     */
    @GetMapping("/selectItemByItemId/{itemId}")
    public AjaxResult selectItemByItemId(@PathVariable Long itemId) {
        return success(itemMapper.selectItemByItemId(itemId));
    }

    /**
     * 发布物品信息(小程序发布页用)
     */
    @PostMapping("/insertItem")
    public AjaxResult insertItem(@RequestBody Item item) {
        return toAjax(itemService.insertItem(item));
    }

    /**
     * 修改物品信息
     */
    @PutMapping("/updateItem")
    public AjaxResult updateItem(@RequestBody Item item) {
        return toAjax(itemService.updateItem(item));
    }

    /**
     * 标记物品为已完成(小程序端, 只能标记自己发布的)
     */
    @PutMapping("/finishItem/{itemId}")
    public AjaxResult finishItem(@PathVariable Long itemId) {
        return toAjax(itemService.finishItem(itemId));
    }

    /**
     * 浏览量+1(小程序详情页打开时调用)
     */
    @PutMapping("/increaseViews/{itemId}")
    public AjaxResult increaseViews(@PathVariable Long itemId) {
        return toAjax(itemService.increaseViews(itemId));
    }

    /**
     * 删除自己发布的物品(小程序端, 只能删自己的)
     */
    @DeleteMapping("/deleteMyItem/{itemId}")
    public AjaxResult deleteMyItem(@PathVariable Long itemId) {
        return toAjax(itemService.deleteMyItem(itemId));
    }

    /**
     * 删除物品信息
     */
    @DeleteMapping("/deleteItemByItemIds/{itemIds}")
    public AjaxResult deleteItemByItemIds(@PathVariable Long[] itemIds) {
        return toAjax(itemService.deleteItemByItemIds(itemIds));
    }
}
