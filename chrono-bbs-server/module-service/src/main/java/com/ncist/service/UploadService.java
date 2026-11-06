package com.ncist.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    /** 上传图片：校验格式/大小，压缩至 800px 宽，上传 OSS，返回访问 URL */
    String uploadImage(MultipartFile file);
}
