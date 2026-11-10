package com.ncist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建会话入参（api-doc 14.3）
 */
@Data
public class ConversationCreateDTO {

    @NotNull(message = "商家 ID 不能为空")
    private Long merchantId;

    private Long productId;
}
