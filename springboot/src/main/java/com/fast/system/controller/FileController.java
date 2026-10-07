package com.fast.system.controller;

import com.fast.system.config.fastConfig;
import com.fast.system.domain.AjaxResult;
import jakarta.annotation.Resource;
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

@RestController
@RequestMapping("/file")
public class FileController extends BaseController {

    @Resource
    private fastConfig fastConfig;

    @PostMapping("/upload")
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = fastConfig.getProfile() + "/file/upload";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String baseName = originalFilename == null ? "" : originalFilename;
            String extension = "";
            if (baseName.contains(".")) {
                extension = baseName.substring(baseName.lastIndexOf("."));
                baseName = baseName.substring(0, baseName.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString().replaceAll("-", "")
                    + "_" + baseName + extension;

            Path filePath = Paths.get(uploadDir, uniqueFilename);
            Files.write(filePath, file.getBytes());

            Map<String, Object> result = new HashMap<>();
            String fileUrl = "/file/upload/" + uniqueFilename;
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