package com.ncist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 图片上传配置属性。
 *
 * <p>绑定配置中的 {@code upload} 前缀。{@code mode=local} 时上传到本地目录，
 * 适合未配置阿里云 OSS 密钥的开发/答辩环境；{@code mode=oss} 时上传到 OSS。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {

    /** 存储模式：local-本地目录，oss-阿里云 OSS。 */
    private String mode = "local";

    /** 本地存储根目录，仅在 {@code mode=local} 时生效。 */
    private String localDir = "./uploads";

    /** 本地文件的访问路径前缀，默认 {@code /uploads}。 */
    private String publicPrefix = "/uploads";

    /**
     * 判断当前是否为本地存储模式。
     *
     * @return {@code true} 表示使用本地目录
     */
    public boolean isLocalMode() {
        return "local".equalsIgnoreCase(mode);
    }
}
