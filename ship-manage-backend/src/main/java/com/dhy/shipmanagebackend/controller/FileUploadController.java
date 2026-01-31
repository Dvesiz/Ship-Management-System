package com.dhy.shipmanagebackend.controller;

import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.utils.S3Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
public class FileUploadController {
    
    @Autowired
    private S3Util s3Util;
    
    @PostMapping({"/upload","/api/upload"})
    public Result<String> upload(MultipartFile file) throws Exception {
        // 1. 参数校验
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        // 2. 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasLength(originalFilename)) {
            return Result.error("文件名不能为空");
        }

        // 3. 安全地获取文件扩展名
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {  // > 0 防止隐藏文件（如 .gitignore）
            extension = originalFilename.substring(lastDotIndex);
        }

        // 4. 生成唯一文件名 (UUID + 后缀)，防止覆盖
        String fileName = UUID.randomUUID().toString() + extension;

        // 5. 调用工具类上传文件
        String url = s3Util.uploadFile(fileName, file.getInputStream());

        // 6. 返回图片 URL 给前端
        return Result.success(url);
    }
}