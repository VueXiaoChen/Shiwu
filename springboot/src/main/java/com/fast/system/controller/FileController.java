package com.fast.system.controller;

import com.fast.system.config.fastConfig;
import com.fast.system.domain.AjaxResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件相关
 */
@RestController
@RequestMapping("/file")
public class FileController extends BaseController {

    /**
     * 文件上传接口
     * 统一保存到 {profile}/upload/ai/ 下，URL 前缀 /upload/ai/
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // ==================== 第一步：统一到 upload/ai 目录 ====================
            String uploadDir = fastConfig.getProfile() + "/upload/ai";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // ==================== 第二步：生成唯一的文件名 ====================
            String originalFilename = file.getOriginalFilename();

            // ★ 先取出不含扩展名的文件名，避免重复加后缀
            String baseName = originalFilename == null ? "" : originalFilename;
            String extension = "";
            if (baseName.contains(".")) {
                extension = baseName.substring(baseName.lastIndexOf("."));
                baseName = baseName.substring(0, baseName.lastIndexOf("."));
            }

            // 文件名 = UUID + "_" + 原始名（不含扩展名） + 扩展名
            String uniqueFilename = UUID.randomUUID().toString().replaceAll("-", "")
                    + "_" + baseName
                    + extension;

            // ==================== 第三步：保存文件 ====================
            Path filePath = Paths.get(uploadDir, uniqueFilename);
            Files.write(filePath, file.getBytes());

            // ==================== 第四步：返回 URL，前缀统一 /upload/ai/ ====================
            Map<String, Object> result = new HashMap<>();
            String fileUrl = "/upload/ai/" + uniqueFilename;
            result.put("url", fileUrl);
            result.put("fileName", fileUrl);
            result.put("newFileName", uniqueFilename);
            result.put("originalFilename", originalFilename);

            return AjaxResult.success(result);

        } catch (IOException e) {
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }
}