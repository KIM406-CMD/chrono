package com.ncist.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ncist.common.enums.UploadErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.config.OssProperties;
import com.ncist.service.UploadService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务（阿里云 OSS）
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int TARGET_WIDTH = 800;

    private final OssProperties ossProperties;
    private volatile OSS ossClient;

    public UploadServiceImpl(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(UploadErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(UploadErrorCode.FILE_TOO_LARGE);
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(UploadErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        try {
            byte[] data = compress(file, extension);
            String objectKey = buildObjectKey(extension);
            ossClient().putObject(ossProperties.getBucketName(), objectKey, new ByteArrayInputStream(data));
            return buildUrl(objectKey);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(UploadErrorCode.UPLOAD_FAILED);
        }
    }

    /**
     * 压缩规则：
     * - jpg/jpeg/png：用 ImageIO 校验真实图片内容；宽 > 800px 时等比缩放，≤800px 原样直传
     * - webp：JDK 原生不支持解码，改用 RIFF/WEBP 魔数头校验，通过后原样直传（不缩放）
     */
    private byte[] compress(MultipartFile file, String extension) throws Exception {
        if ("webp".equals(extension)) {
            // JDK 原生不支持 webp 解码，改用魔数头校验：RIFF....WEBP
            byte[] bytes = file.getBytes();
            if (!isWebp(bytes)) {
                throw new BusinessException(UploadErrorCode.FILE_TYPE_NOT_ALLOWED);
            }
            return bytes;
        }
        BufferedImage src = ImageIO.read(file.getInputStream());
        if (src == null) {
            // 扩展名合法但内容不是真实图片
            throw new BusinessException(UploadErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        if (src.getWidth() <= TARGET_WIDTH) {
            return file.getBytes();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(src)
                .width(TARGET_WIDTH)
                .outputFormat(extension)
                .toOutputStream(out);
        return out.toByteArray();
    }

    /** webp 文件头校验：字节 0-3 为 "RIFF"，8-11 为 "WEBP" */
    private boolean isWebp(byte[] bytes) {
        if (bytes.length < 12) {
            return false;
        }
        return bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }

    private String buildObjectKey(String extension) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "product/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private String buildUrl(String objectKey) {
        // https://{bucket}.{endpoint-host}/{key}
        String endpoint = ossProperties.getEndpoint()
                .replace("https://", "https://" + ossProperties.getBucketName() + ".")
                .replace("http://", "http://" + ossProperties.getBucketName() + ".");
        return endpoint + "/" + objectKey;
    }

    /** 懒加载 OSS 客户端（配置缺失时在首次调用时报错而非启动失败） */
    private OSS ossClient() {
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    ossClient = new OSSClientBuilder().build(
                            ossProperties.getEndpoint(),
                            ossProperties.getAccessKeyId(),
                            ossProperties.getAccessKeySecret());
                }
            }
        }
        return ossClient;
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
