package com.ncist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "图形验证码响应")
public class CaptchaVO {

    @Schema(description = "验证码唯一标识，后续请求需携带此 UUID 以校验验证码", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String uuid;

    @Schema(description = "验证码图片 Base64 编码", example = "data:image/png;base64,iVBORw0KGgo...")
    private String image;
}