package com.example.controller;

import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件相关操作接口
 */
@RestController
@RequestMapping("/files")
public class FileController {

    // 表示本地磁盘文件的存储路径
    @Value("${file.storage-path:../files/}")
    private String filePath;

    private Path resolveFile(String fileName) {
        Path configuredPath = Paths.get(filePath).toAbsolutePath().normalize();
        Path candidate = configuredPath.resolve(fileName).normalize();
        if (candidate.startsWith(configuredPath) && Files.exists(candidate)) {
            return candidate;
        }

        Path workingDirectory = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path alternatePath = workingDirectory.resolve("files").normalize();
        if (alternatePath.startsWith(workingDirectory) && Files.exists(alternatePath.resolve(fileName))) {
            return alternatePath.resolve(fileName).normalize();
        }

        Path parentFilesPath = workingDirectory.resolve("../files").normalize();
        if (parentFilesPath.startsWith(workingDirectory.getParent()) && Files.exists(parentFilesPath.resolve(fileName))) {
            return parentFilesPath.resolve(fileName).normalize();
        }

        return candidate;
    }

    @Value("${fileBaseUrl}")
    private String fileBaseUrl;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        // 文件类型检查 - 新增代码
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        // 获取文件的MIME类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片类型的文件");
        }
        
        // 定义文件的唯一标识
        String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        // 拼接完整的文件存储路径
        Path realFilePath = resolveFile(fileName);
        try {
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            FileUtil.writeBytes(file.getBytes(), realFilePath.toString());
        } catch (IOException e) {
            System.out.println("文件上传错误");
            return Result.error("文件上传失败，请稍后重试");
        }
        String url = fileBaseUrl + "/files/download/" + fileName;
        return Result.success(url);
    }

    /**
     * 文件下载 - 修改为支持图片直接显示
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        // 拼接完整的文件存储路径
        Path realFilePath = resolveFile(fileName);
        
        try {
            // 检查文件是否存在，如果不存在且请求的是默认头像，则提供一个默认的响应
            if (!Files.exists(realFilePath)) {
                // 如果请求的是默认头像但文件不存在
                if ("defaultAvatar.png".equals(fileName)) {
                    response.setStatus(404);
                    response.getWriter().write("默认头像文件不存在");
                    return;
                }
            }
            
            // 设置正确的响应头，让图片能够直接显示而不是下载
            // 根据文件扩展名设置正确的Content-Type
            String fileExt = FileUtil.extName(fileName).toLowerCase();
            String contentType = "image/jpeg"; // 默认类型
            if ("png".equals(fileExt)) {
                contentType = "image/png";
            } else if ("gif".equals(fileExt)) {
                contentType = "image/gif";
            }
            
            response.setContentType(contentType);
            // 移除attachment设置，这样浏览器就会直接显示图片而不是下载
            
            // 通过文件的存储路径拿到文件字节数组
            byte[] bytes = Files.readAllBytes(realFilePath);
            ServletOutputStream os = response.getOutputStream();
            // 将文件字节数组写出到文件流
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            System.out.println("文件下载错误: " + e.getMessage());
            try {
                response.setStatus(500);
                response.getWriter().write("文件处理错误");
            } catch (IOException ex) {
                // 忽略
            }
        }
    }
    
    /**
     * 提供一个简单的方法来检查默认头像是否可用
     */
    @GetMapping("/hasDefaultAvatar")
    public Result hasDefaultAvatar() {
        String defaultAvatarPath = filePath + "defaultAvatar.png";
        return Result.success(FileUtil.exist(defaultAvatarPath));
    }
}