package com.ncist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图形验证码响应。
 *
 * <p>登录、注册、忘记密码等流程前获取图形验证码；uuid 与图片需一同返回前端。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "图形验证码响应")
public class CaptchaVO {

    /** 验证码唯一标识，后续校验请求需携带。 */
    @Schema(description = "验证码唯一标识，后续请求需携带此 UUID 以校验验证码", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String uuid;

    /** 验证码图片 Base64 编码（含 data URI 前缀）。 */
    @Schema(description = "验证码图片 Base64 编码", example = "data:image/png;base64,iVBORw0KGgo...")
    private String image;
}