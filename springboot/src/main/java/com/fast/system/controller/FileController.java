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
     * 这是一个HTTP POST接口，用于接收用户上传的文件
     *
     * 接口地址：POST /upload
     * 请求参数：file - 要上传的文件（通过表单的file字段上传）
     * 返回结果：包含文件访问URL和其他信息的JSON对象
     *
     * @param file Spring自动绑定的上传文件对象，来自表单的"file"字段
     *             MultipartFile封装了文件的所有信息：文件名、大小、内容等
     * @return AjaxResult 统一的API响应格式，包含成功/失败的信息
     */
    @PostMapping("/upload")  // 指定这个方法处理POST请求，路径是/upload
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file) {
        // 使用try-catch捕获可能出现的IO异常，防止程序崩溃
        try {
            // ==================== 第一步：准备上传目录 ====================

            // 从配置文件获取上传的基础路径
            String uploadDir = fastConfig.getProfile() + "/upload";

            // 创建File对象表示上传目录
            File dir = new File(uploadDir);

            // 检查目录是否存在，如果不存在就创建（包括所有不存在的父目录）
            // mkdirs() 和 mkdir() 的区别：
            // - mkdir(): 只能创建单级目录，如果父目录不存在会失败
            // - mkdirs(): 创建多级目录，如果父目录不存在会自动创建
            if (!dir.exists()) {
                dir.mkdirs();  // 创建目录（包括所有必要的父目录）
            }

            // ==================== 第二步：生成唯一的文件名 ====================

            // 获取用户上传文件的原始文件名，比如：avatar.jpg
            String originalFilename = file.getOriginalFilename();

            // 获取文件扩展名（文件后缀），比如：.jpg、.png、.docx等
            String extension = "";  // 默认空字符串
            if (originalFilename.contains(".")) {
                // 从最后一个点号开始截取，获取文件扩展名
                // 注意：这样处理可以正确处理 "my.document.docx" 这样的文件名
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 如果文件名中没有点号，extension保持空字符串

            // 生成一个唯一的文件名，防止文件名冲突
            // UUID.randomUUID() 生成类似 "550e8400-e29b-41d4-a716-446655440000" 的字符串
            // replaceAll("-", "") 去掉UUID中的横线，得到32位纯字符串
            // 组合：UUID + "_" + 原始文件名 + 扩展名
            // 例如：550e8400e29b41d4a716446655440000_avatar.jpg
            String uniqueFilename = UUID.randomUUID().toString().replaceAll("-", "") +
                    "_" + originalFilename +
                    extension;

            // ==================== 第三步：保存文件到服务器 ====================

            // 构建完整的文件保存路径
            // Paths.get() 可以接受多个参数，自动拼接成路径
            // 例如：Paths.get(".file", "aaa_xxx.jpg") → ".file/aaa_xxx.jpg"
            Path filePath = Paths.get(uploadDir, uniqueFilename);

            // 将上传的文件内容写入到指定路径
            // file.getBytes() 获取文件的字节数组
            // Files.write() 将字节数组写入文件（会覆盖已存在的文件）
            Files.write(filePath, file.getBytes());

            // ==================== 第四步：构建返回结果 ====================

            // 创建返回给前端的结果Map
            Map<String, Object> result = new HashMap<>();

            // 计算文件的访问URL
            String fileUrl = "/profile/upload/" + uniqueFilename;

            // 返回给前端的信息
            result.put("url", fileUrl);            // 文件的完整访问URL
            result.put("fileName", fileUrl);       // 文件名（和url相同，前端可能需要这个字段）
            result.put("newFileName", uniqueFilename); // 服务器保存的新文件名
            result.put("originalFilename", originalFilename); // 用户上传的原始文件名

            // 返回成功响应，AjaxResult.success() 通常包含：code=200, msg="操作成功", data=result
            return AjaxResult.success(result);

        } catch (IOException e) {
            // 捕获IO异常（文件读写异常）
            // 例如：磁盘空间不足、没有写入权限、文件过大等

            // 返回错误响应，AjaxResult.error() 通常包含：code=500, msg=错误信息
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }

}
