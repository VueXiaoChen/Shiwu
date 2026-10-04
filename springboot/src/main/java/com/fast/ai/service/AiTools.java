package com.fast.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fast.content.domain.Category;
import com.fast.content.mapper.CategoryMapper;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
import com.fast.system.domain.LoginUser;
import lombok.extern.slf4j.Slf4j;
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
 */
@Slf4j
public class AiTools {

    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;
    private final LoginUser loginUser;
    private final List<Item> matchedItems = new ArrayList<>();
    private final String userImageUrl;

    public AiTools(ItemMapper itemMapper, CategoryMapper categoryMapper,
                   LoginUser loginUser, String userImageUrl) {
        this.itemMapper = itemMapper;
        this.categoryMapper = categoryMapper;
        this.loginUser = loginUser;
        this.userImageUrl = userImageUrl;
    }

    public List<Item> getMatchedItems() {
        Map<Long, Item> unique = new LinkedHashMap<>();
        for (Item it : matchedItems) {
            unique.putIfAbsent(it.getItemId(), it);
        }
        return unique.values().stream().limit(3).toList();
    }

    @Tool(description = "在失物招领平台里检索进行中的失物/招领信息。用户描述丢了什么或捡到什么时必须先调用本工具。" +
            "keywords只传物品本身的关键词(如 耳机、AirPods、保温杯), 地点词一律放location参数, 不要混进keywords。" +
            "type可选: 用户丢了东西传found, 用户捡到东西传lost, 不确定可不传。返回JSON数组, 没有结果时返回空数组[]")
    public String searchItems(
            @ToolParam(description = "物品关键词数组, 只放物品词不放地点") List<String> keywords,
            @ToolParam(description = "偏好的信息类型: lost寻物启事 found失物招领, 可不传", required = false) String type,
            @ToolParam(description = "丢失/拾取地点, 如 图书馆, 可不传", required = false) String location) {
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
            if (keywords != null) {
                for (String kw : keywords) {
                    if (kw == null || kw.isBlank()) continue;
                    String k = kw.trim().toLowerCase();
                    if (title.contains(k)) kwScore += 6;
                    else if (desc.contains(k) || loc.contains(k)) kwScore += 3;
                }
            }
            if (kwScore == 0) continue;
            int score = kwScore;
            if (location != null && !location.isBlank()
                    && (loc.contains(location.trim().toLowerCase()) || desc.contains(location.trim().toLowerCase()))) {
                score += 3;
            }
            if (type != null && type.equals(it.getType())) score += 4;
            hits.add(it);
            scores.put(it.getItemId(), score);
        }
        hits.sort(Comparator.comparingInt((Item it) -> scores.get(it.getItemId())).reversed());
        List<Item> top = hits.stream().limit(5).toList();
        matchedItems.addAll(top);
        return toJsonSummary(top);
    }

    @Tool(description = "当用户上传了物品图片时, 根据图片里识别出的物品特征词在平台里做相似度检索。" +
            "keywords 传视觉模型识别出的特征词数组(颜色、品类、品牌、材质、形状等), " +
            "type 同 searchItems: 用户丢东西传found, 捡东西传lost, 不确定不传。返回JSON数组, 没有结果时返回空数组[]")
    public String searchByImage(
            @ToolParam(description = "图片中识别出的物品特征词数组, 如[\"黑色\",\"耳机\",\"AirPods\"]") List<String> keywords,
            @ToolParam(description = "偏好的信息类型: lost寻物启事 found失物招领, 可不传", required = false) String type) {

        List<Item> candidates = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "open")
                .orderByDesc(Item::getCreateTime)
                .last("limit 200"));

        List<Item> hits = new ArrayList<>();
        Map<Long, Integer> scores = new LinkedHashMap<>();
        Map<Long, Integer> titleHitCounts = new LinkedHashMap<>();
        Map<Long, Integer> totalHitCounts = new LinkedHashMap<>();

        for (Item it : candidates) {
            int score = 0;
            int titleHits = 0;
            int totalHits = 0;
            String title = safe(it.getTitle());
            String desc = safe(it.getDescription());
            String loc = safe(it.getLocation());

            if (keywords != null) {
                for (String kw : keywords) {
                    if (kw == null || kw.isBlank()) continue;
                    String k = kw.trim().toLowerCase();
                    if (k.length() < 2) continue;

                    if (title.contains(k)) {
                        score += 10;
                        titleHits++;
                        totalHits++;
                    } else if (desc.contains(k)) {
                        score += 4;
                        totalHits++;
                    } else if (loc.contains(k)) {
                        score += 1;
                    }
                }
            }

            // 门槛一: 至少命中 2 个特征词
            if (totalHits < 2) continue;
            // 门槛二: 标题至少命中 1 个词
            if (titleHits < 1) continue;

            if (type != null && type.equals(it.getType())) score += 3;
            hits.add(it);
            scores.put(it.getItemId(), score);
            titleHitCounts.put(it.getItemId(), titleHits);
            totalHitCounts.put(it.getItemId(), totalHits);
        }

        hits.sort((a, b) -> {
            int s1 = scores.get(a.getItemId());
            int s2 = scores.get(b.getItemId());
            if (s1 != s2) return s2 - s1;
            return titleHitCounts.get(b.getItemId()) - titleHitCounts.get(a.getItemId());
        });

        List<Item> top = hits.stream().limit(5).toList();
        matchedItems.addAll(top);

        log.info("图片检索 keywords={}, 候选数={}, 返回条数={}", keywords, hits.size(), top.size());
        for (Item it : top) {
            log.info("  候选: title={}, score={}, titleHits={}, totalHits={}",
                    it.getTitle(), scores.get(it.getItemId()),
                    titleHitCounts.get(it.getItemId()),
                    totalHitCounts.get(it.getItemId()));
        }

        return toJsonSummary(top);
    }

    @Tool(description = "查询平台最新发布的进行中失物/招领信息")
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

    @Tool(description = "代用户发布一条寻物启事或失物招领。调用前必须先向用户完整复述要发布的内容并得到用户明确确认。需要用户已登录")
    public String publishItem(
            @ToolParam(description = "信息类型: lost寻物启事 found失物招领") String type,
            @ToolParam(description = "标题") String title,
            @ToolParam(description = "详细描述") String description,
            @ToolParam(description = "物品分类名称") String categoryName,
            @ToolParam(description = "丢失/拾取地点") String location,
            @ToolParam(description = "丢失/拾取日期, yyyy-MM-dd") String happenTime,
            @ToolParam(description = "联系方式") String contact) {
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
        item.setUserId(loginUser.getUserId());
        item.setStatus("open");
        item.setViews(0);
        item.setCreateTime(new Date());
        if (userImageUrl != null && !userImageUrl.isBlank()) {
            item.setImages(userImageUrl);
        }
        try {
            item.setHappenTime(new SimpleDateFormat("yyyy-MM-dd").parse(happenTime));
        } catch (Exception e) {
            item.setHappenTime(new Date());
        }
        itemMapper.insert(item);
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

    @Tool(description = "把当前登录用户自己发布的某条信息标记为已完成。需要用户已登录")
    public String finishMyItem(@ToolParam(description = "要标记的信息ID") Long itemId) {
        if (loginUser == null) {
            return "用户未登录, 无法操作。请引导用户先到小程序「我的」页面登录";
        }
        Item dbItem = itemMapper.selectById(itemId);
        if (dbItem == null) return "这条信息不存在或已被删除";
        if (!dbItem.getUserId().equals(loginUser.getUserId())) return "这条信息不是该用户发布的, 不能操作";
        if ("done".equals(dbItem.getStatus())) return "这条信息已经是完成状态了, 不用重复标记";
        itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getItemId, itemId)
                .set(Item::getStatus, "done"));
        return "已把「" + dbItem.getTitle() + "」标记为已完成";
    }

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
        for (Category c : categories) {
            if (c.getCategoryName().contains("其他")) {
                return c.getCategoryId();
            }
        }
        return categories.isEmpty() ? null : categories.get(0).getCategoryId();
    }

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