package com.fast.ai.service;

import com.fast.item.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class ImageSearchService {

    @Resource private FaceFeatureService faceFeatureService;
    @Resource private ImageFeatureService imageFeatureService;

    private static final float FACE_THRESHOLD = 0.6f;
    private static final float ITEM_THRESHOLD = 0.7f;

    /**
     * 自动路由：先检测人脸，有人脸走人脸识别，无人脸走物品识别
     */
    public List<Item> search(String imagePath, List<Item> candidates) {
        if (faceFeatureService.hasFace(imagePath)) {
            log.info("检测到人脸，走人脸识别通道: {}", imagePath);
            return searchByFace(imagePath, candidates);
        } else {
            log.info("未检测到人脸，走物品图片通道: {}", imagePath);
            return searchByItemImage(imagePath, candidates);
        }
    }

    private List<Item> searchByFace(String imagePath, List<Item> candidates) {
        float[] queryVec = faceFeatureService.extractTopFaceFeature(imagePath);
        List<Item> hits = new ArrayList<>();
        for (Item it : candidates) {
            if (it.getFaceFeature() == null || it.getFaceFeature().isBlank()) continue;
            float[] stored = parseVector(it.getFaceFeature());
            float sim = faceFeatureService.calculateSimilarity(queryVec, stored);
            if (sim >= FACE_THRESHOLD) {
                hits.add(it);
            }
        }
        return hits;
    }

    private List<Item> searchByItemImage(String imagePath, List<Item> candidates) {
        float[] queryVec = imageFeatureService.extractFeatures(imagePath);
        List<Item> hits = new ArrayList<>();
        for (Item it : candidates) {
            if (it.getItemFeature() == null || it.getItemFeature().isBlank()) continue;
            float[] stored = parseVector(it.getItemFeature());
            float sim = imageFeatureService.calculateSimilarity(queryVec, stored);
            if (sim >= ITEM_THRESHOLD) {
                hits.add(it);
            }
        }
        return hits;
    }

    private float[] parseVector(String json) {
        String s = json.replace("[", "").replace("]", "").replace(" ", "");
        String[] parts = s.split(",");
        float[] v = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            v[i] = Float.parseFloat(parts[i]);
        }
        return v;
    }

    public static String toJson(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        return sb.append("]").toString();
    }
}