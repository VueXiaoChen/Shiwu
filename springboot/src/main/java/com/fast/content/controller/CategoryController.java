package com.fast.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.content.domain.Category;
import com.fast.content.mapper.CategoryMapper;
import com.fast.content.service.ICategoryService;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 物品分类信息
 */
@RestController
@RequestMapping("/content/category")
public class CategoryController extends BaseController {
    @Resource
    private ICategoryService categoryService;

    @Resource
    private CategoryMapper categoryMapper;

    /**
     * 查询物品分类列表
     */
    @GetMapping("/selectCategoryList")
    public TableDataInfo selectCategoryList(Category category) {
        Page<Category> page = startPage();
        page = categoryService.selectCategoryList(page, category);
        return getDataTable(page);
    }

    /**
     * 查询所有物品分类(供下拉选择使用)
     */
    @GetMapping("/selectCategoryAll")
    public AjaxResult selectCategoryAll() {
        return success(categoryService.selectCategoryAll());
    }

    /**
     * 根据分类ID查询物品分类信息
     */
    @GetMapping("/selectCategoryByCategoryId/{categoryId}")
    public AjaxResult selectCategoryByCategoryId(@PathVariable Long categoryId) {
        return success(categoryMapper.selectById(categoryId));
    }

    /**
     * 新增物品分类
     */
    @PostMapping("/insertCategory")
    public AjaxResult insertCategory(@RequestBody Category category) {
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改物品分类
     */
    @PutMapping("/updateCategory")
    public AjaxResult updateCategory(@RequestBody Category category) {
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除物品分类
     */
    @DeleteMapping("/deleteCategoryByCategoryIds/{categoryIds}")
    public AjaxResult deleteCategoryByCategoryIds(@PathVariable Long[] categoryIds) {
        return toAjax(categoryService.deleteCategoryByCategoryIds(categoryIds));
    }
}
