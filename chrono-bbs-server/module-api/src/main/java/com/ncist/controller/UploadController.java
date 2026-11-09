package com.ncist.controller;

import com.ncist.common.result.Result;
import com.ncist.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传控制器。
 *
 * <p>提供图片上传能力，仅允许 jpg/jpeg/png/webp 格式且大小不超过 2MB，
 * 服务端按规则压缩（最长边 800px）后上传至对象存储并返回访问 URL。
 * 接口前缀 {@code /api/upload}，要求登录。</p>
 */
@Tag(name = "文件上传", description = "图片上传（jpg/jpeg/png/webp，≤2MB，压缩 800px）")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    /**
     * 上传图片并返回访问 URL。
     *
     * <p>要求登录；空文件、格式不支持或超过 2MB 时由业务层拒绝，
     * 超过 multipart 限制时由全局异常处理器转换为文件过大错误。</p>
     *
     * @param file 待上传的图片文件，支持 jpg/jpeg/png/webp，大小不超过 2MB
     * @return 包含 {@code url} 键的访问地址
     */
    @Operation(summary = "上传图片，返回访问 URL")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = uploadService.uploadImage(file);
        return Result.success(Map.of("url", url));
    }
}
