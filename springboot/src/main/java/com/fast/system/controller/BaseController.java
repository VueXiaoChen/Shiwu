package com.fast.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.system.domain.AjaxResult;
import com.fast.system.domain.TableDataInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * web层通用数据处理
 * 这是所有控制器的"爸爸"(基类), 其他控制器可以继承它
 * 目的: 把常用的方法放在这里, 避免每个控制器重复写
 */
public class BaseController {
    /**
     * 返回成功(无数据)
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回错误(无数据)
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * 返回成功(带消息)
     */
    public AjaxResult success(String msg) {
        return AjaxResult.success(msg);
    }

    /**
     * 返回成功(带数据)
     */
    public AjaxResult success(Object data) {
        return AjaxResult.success(data);
    }

    /**
     * 返回错误(带消息)
     */
    public AjaxResult error(String msg) {
        return AjaxResult.error(msg);
    }

    /**
     * 根据受到影响的行数判断操作是否成功
     * @param rows 数据库操作影响的行数
     * @return 成功或者失败
     * 例子:
     *  int rows = userService.deleteUser(id) //删除用户
     *  return toAjax(rows) //如果rows > 0代表删除成功, 否则返回失败
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 设置请求分页数据，返回 MyBatis Plus 的 Page 对象
     * 使用场景: 在查询列表数据的方法开头调用
     */
    protected <T> Page<T> startPage() {
        //获取当前HTTP请求
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        //获取并且转换分页参数
        int pageNum = Integer.parseInt(req.getParameter("pageNum"));
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));

        // MyBatis Plus 分页对象
        // pageNum: 当前页码
        // pageSize: 每页条数
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 包装 MyBatis Plus 分页结果为前端需要的格式
     */
    protected <T> TableDataInfo getDataTable(Page<T> page) {
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(200);
        dataInfo.setMsg("查询列表成功");
        dataInfo.setRows(page.getRecords());
        dataInfo.setTotal(page.getTotal());
        return dataInfo;
    }

    /**
     * 包装普通列表为分页格式（不需要分页时使用）
     */
    protected <T> TableDataInfo getDataTable(List<T> list) {
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(200);
        dataInfo.setMsg("查询列表成功");
        dataInfo.setRows(list);
        dataInfo.setTotal((long) list.size());
        return dataInfo;
    }
}
