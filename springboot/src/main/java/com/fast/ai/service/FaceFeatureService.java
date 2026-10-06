package com.fast.ai.service;

import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.FaceRecModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import cn.smartjavaai.common.entity.DetectionResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class FaceFeatureService {

    private FaceDetModel faceDetModel;
    private FaceRecModel faceRecModel;

    private static final String MODEL_BASE_PATH = "D:/IDEA/shiwu/models/seetaface6/";

    @PostConstruct
    public void init() {
        // ===== 1. 初始化人脸检测模型 =====
        FaceDetConfig detConfig = new FaceDetConfig();
        detConfig.setModelEnum(FaceDetModelEnum.SEETA_FACE6_MODEL);
        detConfig.setModelPath(MODEL_BASE_PATH);
        detConfig.setConfidenceThreshold(0.9f);
        this.faceDetModel = FaceDetModelFactory.getInstance().getModel(detConfig);

        // ===== 2. 初始化人脸识别模型 =====
        FaceRecConfig recConfig = new FaceRecConfig();
        // ★ 若你库里识别模型枚举名不同，按实际改，常见为 SEETA_FACE6_MODEL
        recConfig.setModelEnum(FaceRecModelEnum.SEETA_FACE6_MODEL);
        recConfig.setModelPath(MODEL_BASE_PATH);
        this.faceRecModel = FaceRecModelFactory.getInstance().getModel(recConfig);

        log.info("SmartJavaAI 人脸检测模型加载完成: {}", this.faceDetModel != null);
        log.info("SmartJavaAI 人脸识别模型加载完成: {}", this.faceRecModel != null);
    }

    /**
     * 快速判断图片中是否包含人脸（用于路由）
     */
    public boolean hasFace(String imagePath) {
        if (faceDetModel == null) {
            log.warn("faceDetModel 未初始化，跳过人脸检测");
            return false;
        }
        // 先确认文件存在，避免底层报模糊错误
        File f = new File(imagePath);
        if (!f.exists()) {
            log.warn("人脸检测跳过：文件不存在 path={}", imagePath);
            return false;
        }
        try {
            R<DetectionResponse> result = faceDetModel.detect(imagePath);
            int count = (result != null && result.isSuccess() && result.getData() != null)
                    ? result.getData().getDetectionInfoList().size() : 0;
            log.info("人脸检测: path={}, success={}, 检测到人脸数={}",
                    imagePath, result != null && result.isSuccess(), count);
            return count > 0;
        } catch (Exception e) {
            log.warn("人脸检测失败: {}", imagePath, e);
            return false;
        }
    }

    /**
     * 提取最高置信度人脸的特征向量
     */
    public float[] extractTopFaceFeature(String imagePath) {
        if (faceRecModel == null) {
            throw new RuntimeException("faceRecModel 未初始化");
        }
        R<float[]> result = faceRecModel.extractTopFaceFeature(imagePath);
        int dim = (result != null && result.getData() != null) ? result.getData().length : 0;
        log.info("人脸特征提取: path={}, success={}, 维度={}",
                imagePath, result != null && result.isSuccess(), dim);
        if (result != null && result.isSuccess() && result.getData() != null) {
            return result.getData();
        }
        throw new RuntimeException("未检测到清晰人脸: " + (result == null ? "null" : result.getMessage()));
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