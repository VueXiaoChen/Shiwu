package com.fast.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.content.domain.Category;
import com.fast.content.mapper.CategoryMapper;
import com.fast.content.service.ICategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 物品分类 业务处理类
 */
@Service
public class CategoryServiceImpl implements ICategoryService {
    @Resource
    private CategoryMapper categoryMapper;

    /**
     * 分页查询物品分类列表
     * @param page 分页对象
     * @param category 查询参数
     * @return 分页结果
     */
    @Override
    public Page<Category> selectCategoryList(Page<Category> page, Category category) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        //分类名称模糊查询
        wrapper.like(StringUtils.isNotBlank(category.getCategoryName()), Category::getCategoryName, category.getCategoryName());
        //按显示排序升序, 排序相同时新建的在前
        wrapper.orderByAsc(Category::getCategorySort).orderByDesc(Category::getCreateTime);
        return categoryMapper.selectPage(page, wrapper);
    }

    /**
     * 查询所有物品分类(不分页, 供下拉选择使用)
     * @return 分类列表
     */
    @Override
    public List<Category> selectCategoryAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getCategorySort);
        return categoryMapper.selectList(wrapper);
    }

    /**
     * 新增物品分类
     * @param category 表单参数
     * @return 是否新增成功
     */
    @Override
    public int insertCategory(Category category) {
        //分类名称不允许重复
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryName, category.getCategoryName()));
        if (count > 0) {
            throw new RuntimeException("分类名称已存在");
        }
        return categoryMapper.insert(category);
    }

    /**
     * 修改物品分类
     * @param category 表单参数
     * @return 是否修改成功
     */
    @Override
    public int updateCategory(Category category) {
        //分类名称不允许与其他分类重复
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryName, category.getCategoryName())
                .ne(Category::getCategoryId, category.getCategoryId()));
        if (count > 0) {
            throw new RuntimeException("分类名称已存在");
        }
        return categoryMapper.updateById(category);
    }

    /**
     * 删除物品分类
     * @param categoryIds 分类ID数组
     * @return 是否删除成功
     */
    @Override
    public int deleteCategoryByCategoryIds(Long[] categoryIds) {
        return categoryMapper.deleteBatchIds(Arrays.asList(categoryIds));
    }
}
