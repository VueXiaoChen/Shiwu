package com.fast.ai.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fast.item.domain.Item;
import com.fast.item.mapper.ItemMapper;
import com.fast.system.config.fastConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 图片特征提取服务
 */
@Slf4j
@Service
public class FeatureExtractService {

    @Resource
    private fastConfig fastConfig;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private FaceFeatureService faceFeatureService;

    @Resource
    private ImageFeatureService imageFeatureService;

    // 图片统一存放目录（与 FileController 保存目录一致）
    private static final String IMAGE_UPLOAD_DIR = "upload/ai/";

    /**
     * 提取图片特征并写入 item 表
     */
    public void extractAndSave(Long itemId, String imageUrl) {
        if (itemId == null || imageUrl == null || imageUrl.isBlank()) {
            log.warn("特征提取跳过：itemId 或 imageUrl 为空 itemId={}, imageUrl={}", itemId, imageUrl);
            return;
        }

        String diskPath = urlToDiskPath(imageUrl);
        log.info("开始提取特征 itemId={}, imageUrl={}, diskPath={}", itemId, imageUrl, diskPath);

        if (diskPath == null || !Files.exists(Paths.get(diskPath))) {
            log.warn("特征提取跳过：文件不存在 itemId={}, path={}", itemId, diskPath);
            return;
        }

        boolean hasFace = false;
        try {
            hasFace = faceFeatureService.hasFace(diskPath);
            log.info("人脸检测结果 itemId={}, hasFace={}", itemId, hasFace);
        } catch (Exception e) {
            log.warn("人脸检测异常，按无人脸处理 itemId={}", itemId, e);
        }

        if (hasFace) {
            try {
                float[] faceVec = faceFeatureService.extractTopFaceFeature(diskPath);
                String faceJson = ImageSearchService.toJson(faceVec);
                int rows = itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                        .eq(Item::getItemId, itemId)
                        .set(Item::getFaceFeature, faceJson));
                log.info("人脸特征已入库 itemId={}, 维度={}, update行数={}",
                        itemId, faceVec.length, rows);
            } catch (Exception e) {
                log.error("人脸特征提取失败 itemId={}", itemId, e);
            }
        } else {
            try {
                float[] itemVec = imageFeatureService.extractFeatures(diskPath);
                String itemJson = ImageSearchService.toJson(itemVec);
                int rows = itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                        .eq(Item::getItemId, itemId)
                        .set(Item::getItemFeature, itemJson));
                log.info("物品特征已入库 itemId={}, 维度={}, update行数={}",
                        itemId, itemVec.length, rows);
            } catch (Exception e) {
                log.error("物品特征提取失败 itemId={}", itemId, e);
            }
        }
    }

    /**
     * URL 转磁盘路径
     * 不管 URL 是 /upload/ai/xxx.png 还是 /profile/upload/xxx.png，
     * 一律取文件名，拼到 {user.dir}/upload/ai/ 下
     */


    private String urlToDiskPath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        String diskPath = Paths.get(fastConfig.getProfile(), "upload", "ai", fileName)
                .normalize().toString();
        log.debug("URL={} → 磁盘路径={}, 存在={}", imageUrl, diskPath, new File(diskPath).exists());
        return diskPath;
    }
}