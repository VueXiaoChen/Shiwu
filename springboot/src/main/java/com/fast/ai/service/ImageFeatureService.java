package com.fast.ai.service;

import cn.smartjavaai.clip.config.ClipModelConfig;
import cn.smartjavaai.clip.enums.ClipModelEnum;
import cn.smartjavaai.clip.model.ClipModel;
import cn.smartjavaai.clip.model.ClipModelFactory;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.R;
import ai.djl.modality.cv.Image;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Slf4j
@Service
public class ImageFeatureService {

    private ClipModel clipModel;

    @PostConstruct
    public void init() {
        ClipModelConfig config = new ClipModelConfig();
        config.setModelEnum(ClipModelEnum.OPENAI);
        config.setModelPath("clip.pt");
        this.clipModel = ClipModelFactory.getInstance().getModel(config);
        log.info("CLIP 图片特征提取模型加载完成");
    }

    /**
     * 提取图片特征（注意：方法名是复数 extractImageFeatures，返回 R 包装对象）
     */
    public float[] extractFeatures(String imagePath) {
        try {
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            R<float[]> result = clipModel.extractImageFeatures(image);
            if (result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
            throw new RuntimeException("特征提取失败: " + result.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("图片特征提取异常: " + imagePath, e);
        }
    }

    /**
     * 计算两个特征向量的余弦相似度（ClipModel 没有 calculateSimilarity 方法，需手动实现）
     */
    public float calculateSimilarity(float[] f1, float[] f2) {
        if (f1 == null || f2 == null || f1.length != f2.length) {
            throw new IllegalArgumentException("特征向量无效或维度不一致");
        }
        float dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < f1.length; i++) {
            dot += f1[i] * f2[i];
            normA += f1[i] * f1[i];
            normB += f2[i] * f2[i];
        }
        float denom = (float) (Math.sqrt(normA) * Math.sqrt(normB));
        return denom == 0 ? 0 : dot / denom;
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (clipModel != null) clipModel.close();
    }
}