package com.ncist.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务。
 *
 * <p>负责将商品图片上传至阿里云 OSS。上传前校验文件非空、大小与扩展名，
 * 并对图片内容做真实性校验；宽度超过 800px 的图片会等比压缩后再上传。</p>
 */
public interface UploadService {

    /**
     * 上传图片。
     *
     * <p>允许 jpg/jpeg/png/webp，文件大小上限 2MB；jpg/jpeg/png 需为可解码的真实图片，
     * webp 通过文件头魔数校验。上传对象按 {@code product/yyyyMMdd/} 日期目录存放。</p>
     *
     * @param file 待上传的图片文件
     * @return 图片访问 URL
     * @throws BusinessException 文件为空、超过大小限制、类型不允许或上传失败时抛出
     */
    String uploadImage(MultipartFile file);
}
