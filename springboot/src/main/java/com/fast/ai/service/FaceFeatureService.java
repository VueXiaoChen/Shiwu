package com.fast.ai.service;

import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.FaceRecModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FaceFeatureService {

    private FaceDetModel faceDetModel;
    private FaceRecModel faceRecModel;

    @PostConstruct
    public void init() {
        FaceRecConfig config = new FaceRecConfig();
        config.setModelPath("models/seetaface6/");
        this.faceDetModel = FaceDetModelFactory.getInstance().getModel();
        this.faceRecModel = FaceRecModelFactory.getInstance().getModel(config);
        log.info("SmartJavaAI 人脸检测与识别模型加载完成");
    }

    /**
     * 快速判断图片中是否包含人脸（用于路由）
     */
    public boolean hasFace(String imagePath) {
        try {
            R<DetectionResponse> result = faceDetModel.detect(imagePath);
            return result.isSuccess()
                    && result.getData() != null
                    && !result.getData().getDetectionInfoList().isEmpty();
        } catch (Exception e) {
            log.warn("人脸检测失败: {}", imagePath, e);
            return false;
        }
    }

    /**
     * 提取最高置信度人脸的特征向量
     */
    public float[] extractTopFaceFeature(String imagePath) {
        R<float[]> result = faceRecModel.extractTopFaceFeature(imagePath);
        if (result.isSuccess() && result.getData() != null) {
            return result.getData();
        }
        throw new RuntimeException("未检测到清晰人脸");
    }

    /**
     * 计算两个人脸特征的相似度
     */
    public float calculateSimilarity(float[] f1, float[] f2) {
        return faceRecModel.calculSimilar(f1, f2);
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (faceDetModel != null) faceDetModel.close();
        if (faceRecModel != null) faceRecModel.close();
    }
}