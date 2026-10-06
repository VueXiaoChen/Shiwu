package com.fast.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fast.ai.domain.SearchKeyword;
import com.fast.ai.mapper.SearchKeywordMapper;
import com.fast.content.domain.Category;
import com.fast.content.mapper.CategoryMapper;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
import com.fast.system.domain.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AiTools {

    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;
    private final SearchKeywordMapper searchKeywordMapper;
    private final LoginUser loginUser;
    private final List<Item> matchedItems = new ArrayList<>();
    private final String userImageUrl;
    private final ImageSearchService imageSearchService;

    // ★ 人脸和物品特征服务
    private final FaceFeatureService faceFeatureService;
    private final ImageFeatureService imageFeatureService;

    public AiTools(ItemMapper itemMapper,
                   CategoryMapper categoryMapper,
                   SearchKeywordMapper searchKeywordMapper,
                   LoginUser loginUser,
                   String userImageUrl,
                   ImageSearchService imageSearchService,
                   FaceFeatureService faceFeatureService,
                   ImageFeatureService imageFeatureService) {
        this.itemMapper = itemMapper;
        this.categoryMapper = categoryMapper;
        this.searchKeywordMapper = searchKeywordMapper;
        this.loginUser = loginUser;
        this.userImageUrl = userImageUrl;
        this.imageSearchService = imageSearchService;
        this.faceFeatureService = faceFeatureService;
        this.imageFeatureService = imageFeatureService;
    }

    public List<Item> getMatchedItems() {
        Map<Long, Item> unique = new LinkedHashMap<>();
        for (Item it : matchedItems) {
            unique.putIfAbsent(it.getItemId(), it);
        }
        return unique.values().stream().limit(50).toList();
    }

    /**
     * 把图片 URL 转成本地磁盘路径
     * 例：/upload/ai/0951a6a8c1e24b78bb11747d3628b978.png
     *  → D:/IDEA/shiwu/upload/ai/0951a6a8c1e24b78bb11747d3628b978.png
     * 保留 URL 里 /upload/ 之后的完整相对路径，不丢中间目录。
     */
    private String urlToDiskPath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        String relative = imageUrl.replaceFirst("^/upload/", "");
        String diskPath = Paths.get(System.getProperty("user.dir"), "upload", relative)
                .normalize()
                .toString();
        log.debug("URL={} → 磁盘路径={}, 存在={}", imageUrl, diskPath, new File(diskPath).exists());
        return diskPath;
    }

    /**
     * 解析出「真实可用」的图片 URL：
     * 优先用构造时注入的 userImageUrl（上传接口返回的真实 URL），
     * 避免 AI 在调用工具时编造文件名。
     */
    private String resolveRealImageUrl(String aiProvidedUrl) {
        if (this.userImageUrl != null && !this.userImageUrl.isBlank()) {
            return this.userImageUrl;
        }
        return aiProvidedUrl;
    }

    /**
     * 提取图片特征并存入数据库
     * - 如果图中有人脸 → 提取人脸特征存入 face_feature
     * - 如果图中无人脸 → 提取物品特征存入 item_feature
     * - 提取失败不影响主流程，只记录日志
     */
    private void extractAndSaveFeature(Long itemId, String imageUrl) {
        log.info("开始提取特征 itemId={}, imageUrl={}", itemId, imageUrl);
        String diskPath = urlToDiskPath(imageUrl);
        log.info("特征提取磁盘路径: {}, 存在={}", diskPath, diskPath != null && Files.exists(Paths.get(diskPath)));

        if (diskPath == null || !Files.exists(Paths.get(diskPath))) {
            log.warn("特征提取跳过：文件不存在 itemId={}", itemId);
            return;
        }

        boolean hasFace = false;
        try {
            hasFace = faceFeatureService.hasFace(diskPath);
            log.info("人脸检测结果 itemId={}, hasFace={}", itemId, hasFace);
        } catch (Exception e) {
            log.warn("人脸检测异常 itemId={}", itemId, e);
        }

        if (hasFace) {
            try {
                float[] faceVec = faceFeatureService.extractTopFaceFeature(diskPath);
                String faceJson = ImageSearchService.toJson(faceVec);
                itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                        .eq(Item::getItemId, itemId)
                        .set(Item::getFaceFeature, faceJson));
                log.info("人脸特征已入库 itemId={}, 维度={}", itemId, faceVec.length);
            } catch (Exception e) {
                log.error("人脸特征提取失败 itemId={}", itemId, e);
            }
        } else {
            try {
                float[] itemVec = imageFeatureService.extractFeatures(diskPath);
                String itemJson = ImageSearchService.toJson(itemVec);
                itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                        .eq(Item::getItemId, itemId)
                        .set(Item::getItemFeature, itemJson));
                log.info("物品特征已入库 itemId={}, 维度={}", itemId, itemVec.length);
            } catch (Exception e) {
                log.error("物品特征提取失败 itemId={}", itemId, e);
            }
        }
    }

    /**
     * 关键词搜索（支持动态关键词 + 人物标签）
     */
    @Tool(description = "在失物招领平台里检索进行中的失物/招领信息。" +
            "keywords 可以是物品词(耳机/AirPods)或人物特征词(美女/帅哥)，" +
            "地点词一律放 location 参数。type 可选: lost/found。返回 JSON 数组。")
    public String searchItems(
            @ToolParam(description = "关键词数组") List<String> keywords,
            @ToolParam(description = "类型: lost寻物启事 found失物招领", required = false) String type,
            @ToolParam(description = "地点", required = false) String location) {

        List<SearchKeyword> dynamicKws = searchKeywordMapper.selectList(
                new LambdaQueryWrapper<SearchKeyword>()
                        .eq(SearchKeyword::getEnabled, 1)
                        .orderByAsc(SearchKeyword::getSortOrder));
        Set<String> allKeywords = new LinkedHashSet<>();
        if (keywords != null) allKeywords.addAll(keywords);
        for (SearchKeyword dk : dynamicKws) allKeywords.add(dk.getKeyword());

        List<Item> candidates = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "open")
                .orderByDesc(Item::getCreateTime)
                .last("limit 500"));

        List<Item> hits = new ArrayList<>();
        Map<Long, Integer> scores = new LinkedHashMap<>();

        for (Item it : candidates) {
            int kwScore = 0;
            String title = safe(it.getTitle());
            String desc = safe(it.getDescription());
            String loc = safe(it.getLocation());
            String tags = safe(it.getPersonTags());

            for (String kw : allKeywords) {
                if (kw == null || kw.isBlank()) continue;
                String k = kw.trim().toLowerCase();
                if (title.contains(k)) kwScore += 6;
                else if (desc.contains(k) || loc.contains(k)) kwScore += 3;
                else if (tags.contains(k)) kwScore += 5;
            }
            if (kwScore == 0) continue;

            int score = kwScore;
            if (location != null && !location.isBlank()
                    && (loc.contains(location.trim().toLowerCase())
                    || desc.contains(location.trim().toLowerCase()))) {
                score += 3;
            }
            if (type != null && type.equals(it.getType())) score += 4;
            hits.add(it);
            scores.put(it.getItemId(), score);
        }
        hits.sort(Comparator.comparingInt(
                (Item it) -> scores.get(it.getItemId())).reversed());
        List<Item> top = hits.stream().limit(50).toList();
        matchedItems.addAll(top);
        return toJsonSummary(top);
    }

    /**
     * 以图搜图（自动路由：人脸 / 物品）
     * ★ 关键改动：优先使用 this.userImageUrl（真实上传 URL），忽略 AI 可能编造的 imageUrl
     */
    @Tool(description = "用户上传图片时调用。系统会自动判断图片中是否有人脸：" +
            "有人脸走人脸识别，无人脸走物品图片识别。返回 JSON 数组。")
    public String searchByImageVector(
            @ToolParam(description = "图片URL，如 /upload/ai/xxx.jpg") String imageUrl,
            @ToolParam(description = "类型: lost/found，可不传", required = false) String type) {

        try {
            // ★ 优先用真实上传的 URL，避免 AI 编造文件名
            String realUrl = resolveRealImageUrl(imageUrl);
            log.info("以图搜图使用 URL: {} (AI传入={})", realUrl, imageUrl);

            String diskPath = urlToDiskPath(realUrl);
            log.info("以图搜图磁盘路径: {}", diskPath);

            if (diskPath == null || !Files.exists(Paths.get(diskPath))) {
                log.warn("以图搜图跳过：文件不存在, diskPath={}", diskPath);
                return "[]";
            }

            List<Item> candidates = itemMapper.selectList(
                    new LambdaQueryWrapper<Item>()
                            .eq(Item::getStatus, "open")
                            .orderByDesc(Item::getCreateTime)
                            .last("limit 500"));

            List<Item> hits = imageSearchService.search(diskPath, candidates);
            if (type != null && !type.isBlank()) {
                hits = hits.stream()
                        .filter(it -> type.equals(it.getType()))
                        .toList();
            }
            List<Item> top = hits.stream().limit(10).toList();
            matchedItems.addAll(top);
            log.info("以图搜图完成: realUrl={}, 返回条数={}", realUrl, top.size());
            return toJsonSummary(top);
        } catch (Exception e) {
            log.error("以图搜图失败: imageUrl={}", imageUrl, e);
            return "[]";
        }
    }

    @Tool(description = "查询平台最新发布的进行中失物/招领信息")
    public String getRecentItems(
            @ToolParam(description = "类型: lost/found，不传查全部", required = false) String type,
            @ToolParam(description = "条数, 默认5, 最多10", required = false) Integer limit) {
        int size = (limit == null || limit <= 0) ? 5 : Math.min(limit, 10);
        List<Item> list = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "open")
                .eq(type != null && !type.isBlank(), Item::getType, type)
                .orderByDesc(Item::getCreateTime)
                .last("limit " + size));
        matchedItems.addAll(list);
        return toJsonSummary(list);
    }

    @Tool(description = "代用户发布寻物启事或失物招领。调用前必须得到用户明确确认。")
    public String publishItem(
            @ToolParam(description = "类型: lost/found") String type,
            @ToolParam(description = "标题") String title,
            @ToolParam(description = "详细描述") String description,
            @ToolParam(description = "分类名称") String categoryName,
            @ToolParam(description = "地点") String location,
            @ToolParam(description = "日期 yyyy-MM-dd") String happenTime,
            @ToolParam(description = "联系方式") String contact,
            @ToolParam(description = "人物特征标签，如 美女,长发，没有可不传", required = false) String personTags) {

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
        if (personTags != null && !personTags.isBlank()) {
            item.setPersonTags(personTags);
        }

        try {
            item.setHappenTime(new SimpleDateFormat("yyyy-MM-dd").parse(happenTime));
        } catch (Exception e) {
            item.setHappenTime(new Date());
        }

        itemMapper.insert(item);
        Long itemId = item.getItemId();

        if (userImageUrl != null && !userImageUrl.isBlank()) {
            extractAndSaveFeature(itemId, userImageUrl);
        }

        matchedItems.add(item);
        return "发布成功, 信息ID为" + itemId + ", 已经可以在平台里被其他同学看到了";
    }

    @Tool(description = "查询当前登录用户自己发布过的失物/寻物信息列表。")
    public String getMyItems(
            @ToolParam(description = "状态: open/done，不传查全部", required = false) String status) {
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

    @Tool(description = "把当前登录用户自己发布的某条信息标记为已完成。")
    public String finishMyItem(@ToolParam(description = "信息ID") Long itemId) {
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