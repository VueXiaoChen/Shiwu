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

@Slf4j
@Service
public class FeatureExtractService {

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private FaceFeatureService faceFeatureService;

    @Resource
    private ImageFeatureService imageFeatureService;

    @Resource
    private fastConfig fastConfig;

    public void extractAndSave(Long itemId, String imageUrl) {
        if (itemId == null || imageUrl == null || imageUrl.isBlank()) {
            log.warn("特征提取跳过：itemId 或 imageUrl 为空");
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
            log.warn("人脸检测异常 itemId={}", itemId, e);
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

    private String urlToDiskPath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        String diskPath = Paths.get(fastConfig.getProfile(), "file", "upload", fileName)
                .normalize().toString();
        log.debug("URL={} → 磁盘路径={}, 存在={}", imageUrl, diskPath, new File(diskPath).exists());
        return diskPath;
    }
}