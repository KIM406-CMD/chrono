package com.ncist.unit.service;

import com.ncist.common.enums.UploadErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.config.OssProperties;
import com.ncist.config.UploadProperties;
import com.ncist.service.impl.UploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UploadServiceImpl 单元测试（本地存储模式）。
 */
@DisplayName("文件上传服务单元测试")
class UploadServiceImplTest {

    @TempDir
    Path tempDir;

    private UploadServiceImpl uploadService;
    private UploadProperties uploadProperties;

    @BeforeEach
    void setUp() {
        uploadProperties = new UploadProperties();
        uploadProperties.setMode("local");
        uploadProperties.setLocalDir(tempDir.toString());
        uploadProperties.setPublicPrefix("/uploads");
        uploadService = new UploadServiceImpl(new OssProperties(), uploadProperties);
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    @Test
    @DisplayName("本地模式-上传 PNG 成功并写入目标目录")
    void uploadLocalSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngBytes());

        String url = uploadService.uploadImage(file);

        assertTrue(url.startsWith("/uploads/product/"));
        Path saved = tempDir.resolve(url.substring("/uploads/".length()));
        assertTrue(Files.exists(saved));
        assertTrue(Files.size(saved) > 0);
    }

    @Test
    @DisplayName("上传-扩展名不允许返回业务错误")
    void uploadExtensionNotAllowed() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());

        BusinessException e = assertThrows(BusinessException.class,
                () -> uploadService.uploadImage(file));

        assertEquals(UploadErrorCode.FILE_TYPE_NOT_ALLOWED.getCode(), e.getCode());
    }

    @Test
    @DisplayName("上传-扩展名合法但内容不是图片时拒绝")
    void uploadFakeImage() {
        MockMultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", "not-an-image".getBytes());

        BusinessException e = assertThrows(BusinessException.class,
                () -> uploadService.uploadImage(file));

        assertEquals(UploadErrorCode.FILE_TYPE_NOT_ALLOWED.getCode(), e.getCode());
    }

    @Test
    @DisplayName("上传-空文件被拒绝")
    void uploadEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        BusinessException e = assertThrows(BusinessException.class,
                () -> uploadService.uploadImage(file));

        assertEquals(UploadErrorCode.FILE_EMPTY.getCode(), e.getCode());
    }
}
