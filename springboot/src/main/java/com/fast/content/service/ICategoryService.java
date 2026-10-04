package com.fast.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.content.domain.Category;

import java.util.List;

/**
 * 物品分类 业务接口
 */
public interface ICategoryService {

    /**
     * 分页查询物品分类列表
     * @param page 分页对象
     * @param category 查询参数
     * @return 分页结果
     */
    Page<Category> selectCategoryList(Page<Category> page, Category category);

    /**
     * 查询所有物品分类(不分页, 供下拉选择使用)
     * @return 分类列表
     */
    List<Category> selectCategoryAll();

    /**
     * 新增物品分类
     * @param category 表单参数
     * @return 是否新增成功
     */
    int insertCategory(Category category);

    /**
     * 修改物品分类
     * @param category 表单参数
     * @return 是否修改成功
     */
    int updateCategory(Category category);

    /**
     * 删除物品分类
     * @param categoryIds 分类ID数组
     * @return 是否删除成功
     */
    int deleteCategoryByCategoryIds(Long[] categoryIds);
}
