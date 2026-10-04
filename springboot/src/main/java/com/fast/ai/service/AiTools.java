package com.fast.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fast.content.domain.Category;
import com.fast.content.mapper.CategoryMapper;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
import com.fast.system.domain.LoginUser;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI找物Agent的工具箱 — 注册给大模型自主调用
 *
 * 注意: 这个类不是Spring Bean, 每次对话请求都会 new 一个新实例,
 * 原因有两个:
 * 1. matchedItems 收集本次对话命中的物品, 实例隔离天然线程安全
 * 2. SSE流式回复在异步线程执行, SecurityContext的ThreadLocal取不到用户,
 *    所以登录用户在创建实例时就传进来存住
 */
public class AiTools {

    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;
    //当前登录用户(未登录为null, 需要登录的工具会拦下来)
    private final LoginUser loginUser;
    //本次对话中工具命中/新发布的物品, 流结束后取出来当卡片返回给小程序
    private final List<Item> matchedItems = new ArrayList<>();

    public AiTools(ItemMapper itemMapper, CategoryMapper categoryMapper, LoginUser loginUser) {
        this.itemMapper = itemMapper;
        this.categoryMapper = categoryMapper;
        this.loginUser = loginUser;
    }

    /**
     * 取出本次对话涉及的物品(按itemId去重, 最多3条), 给前端渲染卡片用
     */
    public List<Item> getMatchedItems() {
        Map<Long, Item> unique = new LinkedHashMap<>();
        for (Item it : matchedItems) {
            unique.putIfAbsent(it.getItemId(), it);
        }
        return unique.values().stream().limit(3).toList();
    }

    @Tool(description = "在失物招领平台里检索进行中的失物/招领信息。用户描述丢了什么或捡到什么时必须先调用本工具。" +
            "keywords只传物品本身的关键词(如 耳机、AirPods、保温杯), 地点词一律放location参数, 不要混进keywords。" +
            "type可选: 用户丢了东西传found(优先看别人捡到的招领信息), " +
            "用户捡到东西传lost(优先看别人发的寻物启事), 不确定可不传。返回JSON数组, 没有结果时返回空数组[]")
    public String searchItems(
            @ToolParam(description = "物品关键词数组, 只放物品词不放地点, 如[\"耳机\",\"AirPods\"]") List<String> keywords,
            @ToolParam(description = "偏好的信息类型: lost寻物启事 found失物招领, 可不传", required = false) String type,
            @ToolParam(description = "丢失/拾取地点, 如 图书馆, 可不传", required = false) String location) {
        //先把进行中的信息捞出来(最多近200条), 在Java里打分排序
        List<Item> candidates = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "open")
                .orderByDesc(Item::getCreateTime)
                .last("limit 200"));

        List<Item> hits = new ArrayList<>();
        Map<Long, Integer> scores = new LinkedHashMap<>();
        for (Item it : candidates) {
            int kwScore = 0;
            String title = safe(it.getTitle());
            String desc = safe(it.getDescription());
            String loc = safe(it.getLocation());
            //关键词命中标题+6, 命中描述/地点+3
            if (keywords != null) {
                for (String kw : keywords) {
                    if (kw == null || kw.isBlank()) continue;
                    String k = kw.trim().toLowerCase();
                    if (title.contains(k)) kwScore += 6;
                    else if (desc.contains(k) || loc.contains(k)) kwScore += 3;
                }
            }
            //一个物品关键词都没命中就不算匹配, 光地点对得上没意义(操场丢无人机不能拿钥匙凑数)
            if (kwScore == 0) continue;
            int score = kwScore;
            //地点命中+3
            if (location != null && !location.isBlank()
                    && (loc.contains(location.trim().toLowerCase()) || desc.contains(location.trim().toLowerCase()))) {
                score += 3;
            }
            //类型和偏好一致+4(丢东西的人优先看招领, 捡东西的人优先看寻物)
            if (type != null && type.equals(it.getType())) score += 4;
            hits.add(it);
            scores.put(it.getItemId(), score);
        }
        hits.sort(Comparator.comparingInt((Item it) -> scores.get(it.getItemId())).reversed());
        List<Item> top = hits.stream().limit(5).toList();
        //命中的物品记下来, 结束后给前端出卡片
        matchedItems.addAll(top);
        return toJsonSummary(top);
    }

    @Tool(description = "查询平台最新发布的进行中失物/招领信息, 用于回答\"最近有人捡到什么\"\"最新的寻物启事\"这类浏览型问题")
    public String getRecentItems(
            @ToolParam(description = "信息类型: lost寻物启事 found失物招领, 不传查全部", required = false) String type,
            @ToolParam(description = "查询条数, 默认5, 最多10", required = false) Integer limit) {
        int size = (limit == null || limit <= 0) ? 5 : Math.min(limit, 10);
        List<Item> list = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "open")
                .eq(type != null && !type.isBlank(), Item::getType, type)
                .orderByDesc(Item::getCreateTime)
                .last("limit " + size));
        matchedItems.addAll(list);
        return toJsonSummary(list);
    }

    @Tool(description = "代用户发布一条寻物启事或失物招领。调用前必须先向用户完整复述要发布的内容并得到用户明确确认, " +
            "信息不全时不允许编造, 缺什么就先追问用户。需要用户已登录")
    public String publishItem(
            @ToolParam(description = "信息类型: lost寻物启事(用户丢了东西) found失物招领(用户捡到东西)") String type,
            @ToolParam(description = "标题, 简短概括物品, 如: 黑色AirPods Pro耳机") String title,
            @ToolParam(description = "详细描述: 物品特征、丢失/拾取经过等") String description,
            @ToolParam(description = "物品分类名称, 如: 电子产品、证件卡片、书籍资料, 根据物品自行判断") String categoryName,
            @ToolParam(description = "丢失/拾取地点") String location,
            @ToolParam(description = "丢失/拾取日期, 格式yyyy-MM-dd, 用户没说就传今天") String happenTime,
            @ToolParam(description = "联系方式(手机号/微信号等)") String contact) {
        if (loginUser == null) {
            return "用户未登录, 无法发布。请引导用户先到小程序「我的」页面登录后再来找我发布";
        }
        Item item = new Item();
        item.setType("found".equals(type) ? "found" : "lost");
        item.setTitle(title);
        item.setDescription(description);
        item.setCategoryId(matchCategoryId(categoryName));
        item.setLocation(location);
        item.setContact(contact);
        item.setUrgent(0);
        //AI代发布拿不到ThreadLocal里的登录态, 直接用创建工具箱时存的用户
        item.setUserId(loginUser.getUserId());
        item.setStatus("open");
        item.setViews(0);
        item.setCreateTime(new Date());
        try {
            item.setHappenTime(new SimpleDateFormat("yyyy-MM-dd").parse(happenTime));
        } catch (Exception e) {
            item.setHappenTime(new Date());
        }
        itemMapper.insert(item);
        //新发布的也记进卡片列表, 用户能直接点进详情看
        matchedItems.add(item);
        return "发布成功, 信息ID为" + item.getItemId() + ", 已经可以在平台里被其他同学看到了";
    }

    @Tool(description = "查询当前登录用户自己发布过的失物/寻物信息列表。需要用户已登录")
    public String getMyItems(
            @ToolParam(description = "状态筛选: open进行中 done已完成, 不传查全部", required = false) String status) {
        if (loginUser == null) {
            return "用户未登录, 查不到发布记录。请引导用户先到小程序「我的」页面登录";
        }
        List<Item> list = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .eq(Item::getUserId, loginUser.getUserId())
                .eq(status != null && !status.isBlank(), Item::getStatus, status)
                .orderByDesc(Item::getCreateTime)
                .last("limit 10"));
        matchedItems.addAll(list);
        return toJsonSummary(list);
    }

    @Tool(description = "把当前登录用户自己发布的某条信息标记为已完成(已找回/已归还)。" +
            "调用前必须先和用户确认是哪一条(物品名称), itemId从getMyItems的结果里取。需要用户已登录")
    public String finishMyItem(@ToolParam(description = "要标记的信息ID") Long itemId) {
        if (loginUser == null) {
            return "用户未登录, 无法操作。请引导用户先到小程序「我的」页面登录";
        }
        Item dbItem = itemMapper.selectById(itemId);
        if (dbItem == null) {
            return "这条信息不存在或已被删除";
        }
        //只能动自己发的, 别人的不行
        if (!dbItem.getUserId().equals(loginUser.getUserId())) {
            return "这条信息不是该用户发布的, 不能操作";
        }
        if ("done".equals(dbItem.getStatus())) {
            return "这条信息已经是完成状态了, 不用重复标记";
        }
        itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getItemId, itemId)
                .set(Item::getStatus, "done"));
        return "已把「" + dbItem.getTitle() + "」标记为已完成";
    }

    /**
     * 分类名称模糊匹配分类表, 匹配不到就落到"其他物品"
     */
    private Long matchCategoryId(String categoryName) {
        List<Category> categories = categoryMapper.selectList(null);
        if (categoryName != null && !categoryName.isBlank()) {
            String name = categoryName.trim();
            for (Category c : categories) {
                if (c.getCategoryName().contains(name) || name.contains(c.getCategoryName())) {
                    return c.getCategoryId();
                }
            }
        }
        //兜底找"其他"分类, 连"其他"都没有就取第一个
        for (Category c : categories) {
            if (c.getCategoryName().contains("其他")) {
                return c.getCategoryId();
            }
        }
        return categories.isEmpty() ? null : categories.get(0).getCategoryId();
    }

    /**
     * 把物品列表压成给模型看的JSON摘要(描述截断, 省token)
     */
    private String toJsonSummary(List<Item> list) {
        if (list == null || list.isEmpty()) return "[]";
        SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Item it = list.get(i);
            String desc = it.getDescription() == null ? "" : it.getDescription();
            if (desc.length() > 40) desc = desc.substring(0, 40) + "...";
            if (i > 0) sb.append(",");
            sb.append("{\"itemId\":").append(it.getItemId())
                    .append(",\"type\":\"").append("found".equals(it.getType()) ? "失物招领" : "寻物启事")
                    .append("\",\"title\":\"").append(esc(it.getTitle()))
                    .append("\",\"location\":\"").append(esc(it.getLocation()))
                    .append("\",\"happenTime\":\"").append(it.getHappenTime() == null ? "" : day.format(it.getHappenTime()))
                    .append("\",\"status\":\"").append("open".equals(it.getStatus()) ? "进行中" : "已完成")
                    .append("\",\"desc\":\"").append(esc(desc)).append("\"}");
        }
        return sb.append("]").toString();
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}
