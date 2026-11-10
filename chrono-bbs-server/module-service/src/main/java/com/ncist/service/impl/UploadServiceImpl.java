package com.ncist.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ncist.common.enums.UploadErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.config.OssProperties;
import com.ncist.config.UploadProperties;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务实现（本地目录 / 阿里云 OSS）。
 *
 * <p>上传前校验文件非空、大小与扩展名，并对图片内容做真实性校验；
 * 宽度超过 800px 的 jpg/jpeg/png 会等比压缩后上传，webp 不缩放。
 * {@code upload.mode=local} 时写入本地目录并由静态资源映射对外访问，
 * 适合未配置 OSS 密钥的开发环境；{@code mode=oss} 时上传至 OSS，
 * 客户端采用双重检查锁懒加载，应用关闭时通过 {@code @PreDestroy} 释放连接。</p>
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    /** 允许上传的图片扩展名（小写）。 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    /** 单文件大小上限：2MB。 */
    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB
    /** 图片压缩目标宽度（像素），小于该宽度不缩放。 */
    private static final int TARGET_WIDTH = 800;

    /** 阿里云 OSS 配置。 */
    private final OssProperties ossProperties;
    /** 上传模式与本地目录配置。 */
    private final UploadProperties uploadProperties;
    /** OSS 客户端，首次上传时懒加载；使用 volatile 保证双重检查锁的可见性。 */
    private volatile OSS ossClient;

    /**
     * 构造上传服务。
     *
     * @param ossProperties   阿里云 OSS 配置
     * @param uploadProperties 上传模式与本地目录配置
     */
    public UploadServiceImpl(OssProperties ossProperties, UploadProperties uploadProperties) {
        this.ossProperties = ossProperties;
        this.uploadProperties = uploadProperties;
    }

    /**
     * {@inheritDoc}
     *
     * <p>校验通过后按扩展名选择压缩策略，生成日期目录下的随机对象名并上传，
     * 最终返回拼接后的访问 URL。</p>
     */
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
            if (uploadProperties.isLocalMode()) {
                writeLocal(data, objectKey);
                String prefix = uploadProperties.getPublicPrefix() == null
                        ? "/uploads" : uploadProperties.getPublicPrefix();
                if (prefix.endsWith("/")) {
                    prefix = prefix.substring(0, prefix.length() - 1);
                }
                return prefix + "/" + objectKey;
            }
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

    /**
     * 生成 OSS 对象名：{@code product/yyyyMMdd/{uuid}.{ext}}。
     *
     * @param extension 文件扩展名
     * @return OSS 对象 key
     */
    private String buildObjectKey(String extension) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "product/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    /**
     * 将图片写入本地存储目录，目录不存在时自动创建。
     *
     * @param data      图片字节内容
     * @param objectKey 相对对象名，形如 {@code product/yyyyMMdd/{uuid}.{ext}}
     * @throws Exception 目标路径越界或写入失败时抛出
     */
    private void writeLocal(byte[] data, String objectKey) throws Exception {
        Path root = Paths.get(uploadProperties.getLocalDir()).toAbsolutePath().normalize();
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(UploadErrorCode.UPLOAD_FAILED);
        }
        Files.createDirectories(target.getParent());
        Files.write(target, data);
    }

    /**
     * 根据 endpoint 与对象 key 拼接访问 URL，bucket 作为子域名。
     *
     * @param objectKey OSS 对象 key
     * @return 图片访问 URL
     */
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

    /**
     * 应用关闭时释放 OSS 客户端连接。
     */
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
