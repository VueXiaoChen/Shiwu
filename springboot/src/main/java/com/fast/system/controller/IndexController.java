package com.fast.system.controller;

import com.fast.system.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器, 处理根路径请求
 */
@RestController //标记这个类是一个REST API控制器 (它会自动把返回值转换为json格式)
@RequestMapping("/")
public class IndexController extends BaseController{
    @GetMapping
    public AjaxResult home() {
        return success("恭喜你, 成功启动了后端~");
    }

}
