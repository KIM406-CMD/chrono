package com.ncist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性。
 *
 * <p>绑定配置文件中的 {@code aliyun.oss} 前缀（环境变量注入，见 application.yaml），
 * 供商品图片上传服务初始化 OSS 客户端与拼接访问 URL 使用。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /** OSS 服务访问域名，例如 {@code https://oss-cn-hangzhou.aliyuncs.com}。 */
    private String endpoint;

    /** 阿里云访问密钥 ID。 */
    private String accessKeyId;

    /** 阿里云访问密钥 Secret。 */
    private String accessKeySecret;

    /** 存储空间名称（bucket），用于上传对象与拼接访问 URL。 */
    private String bucketName;
}
