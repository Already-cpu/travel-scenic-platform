package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.common.AuthUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private final Path uploadRoot = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();

    @PostMapping("/image")
    public ApiResponse<String> image(@RequestPart("file") MultipartFile file) throws IOException {
        if (!AuthUtils.isAuthenticated()) throw new com.example.travel.common.BusinessException(401, "请先登录");
        if (file == null || file.isEmpty()) return ApiResponse.error(500, "请选择图片文件");
        if (file.getSize() > MAX_IMAGE_SIZE) return ApiResponse.error(500, "图片必须小于 10MB");
        byte[] bytes = file.getBytes();
        String extension = extension(bytes);
        if (extension == null) return ApiResponse.error(500, "请上传有效的 JPG、PNG、GIF 或 WebP 图片");
        Files.createDirectories(uploadRoot);
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Files.write(uploadRoot.resolve(fileName), bytes);
        return ApiResponse.success("/uploads/" + fileName);
    }

    // Detect the actual binary signature; never serve uploaded HTML or SVG as images.
    private String extension(byte[] b) {
        if (b.length < 12) return null;
        if ((b[0]&255)==255 && (b[1]&255)==216 && (b[2]&255)==255) return ".jpg";
        if ((b[0]&255)==137 && b[1]==80 && b[2]==78 && b[3]==71 && b[4]==13 && b[5]==10 && b[6]==26 && b[7]==10) return ".png";
        if (b[0]=='G' && b[1]=='I' && b[2]=='F' && b[3]=='8' && (b[4]=='7'||b[4]=='9') && b[5]=='a') return ".gif";
        if (b[0]=='R' && b[1]=='I' && b[2]=='F' && b[3]=='F' && b[8]=='W' && b[9]=='E' && b[10]=='B' && b[11]=='P') return ".webp";
        return null;
    }
}
