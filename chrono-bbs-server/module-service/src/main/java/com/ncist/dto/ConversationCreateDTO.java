package com.ncist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建会话入参（api-doc 14.3）
 */
@Data
public class ConversationCreateDTO {

    /** 商家用户的用户 ID（merchant 表的 user_id），不是商家表主键。 */
    @NotNull(message = "商家用户 ID 不能为空")
    private Long merchantId;

    private Long productId;
}
