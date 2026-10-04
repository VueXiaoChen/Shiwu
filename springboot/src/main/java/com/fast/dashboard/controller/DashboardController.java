package com.fast.dashboard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fast.dashboard.mapper.DashboardMapper;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
import com.fast.message.mapper.MessageMapper;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import com.fast.system.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏统计（管理端首页用）
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {
    @Resource
    private DashboardMapper dashboardMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageMapper messageMapper;

    /**
     * 大屏统计数据一把梭: 计数 + 近7天趋势 + 分类分布 + 浏览TOP5 + 最新发布
     */
    @GetMapping("/stats")
    public AjaxResult stats() {
        AjaxResult ajax = success();

        //基础计数(物品/用户/消息)
        ajax.put("itemTotal", itemMapper.selectCount(null));
        ajax.put("lostCount", itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getType, "lost")));
        ajax.put("foundCount", itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getType, "found")));
        ajax.put("openCount", itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getStatus, "open")));
        ajax.put("doneCount", itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getStatus, "done")));
        ajax.put("userCount", userMapper.selectCount(null));
        ajax.put("messageCount", messageMapper.selectCount(null));
        //今日新增(发布时间是今天的物品数)
        ajax.put("todayNewCount", itemMapper.selectCount(new LambdaQueryWrapper<Item>().apply("date(create_time) = curdate()")));

        //近7天趋势: SQL只返回有发布的日期, 这里补全7天并把没数据的天填0
        List<Map<String, Object>> trendRows = dashboardMapper.selectTrend7d();
        Map<String, Map<String, Object>> trendByDay = new HashMap<>();
        for (Map<String, Object> row : trendRows) {
            trendByDay.put(String.valueOf(row.get("day")), row);
        }
        List<Map<String, Object>> trend7d = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            String day = LocalDate.now().minusDays(i).format(formatter);
            Map<String, Object> row = trendByDay.get(day);
            Map<String, Object> point = new HashMap<>();
            point.put("day", day);
            point.put("lost", row == null ? 0L : ((Number) row.get("lost")).longValue());
            point.put("found", row == null ? 0L : ((Number) row.get("found")).longValue());
            trend7d.add(point);
        }
        ajax.put("trend7d", trend7d);

        //分类分布
        ajax.put("categoryDist", dashboardMapper.selectCategoryDist());

        //浏览量TOP5(只取标题和浏览量两列)
        ajax.put("viewsTop5", itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .select(Item::getTitle, Item::getViews)
                .orderByDesc(Item::getViews)
                .last("limit 5")));

        //最新发布的8条(滚动列表用)
        ajax.put("latestItems", itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .select(Item::getItemId, Item::getType, Item::getTitle, Item::getLocation, Item::getCreateTime)
                .orderByDesc(Item::getCreateTime)
                .last("limit 8")));

        return ajax;
    }
}
