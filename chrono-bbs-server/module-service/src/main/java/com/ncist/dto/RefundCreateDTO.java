package com.ncist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 申请退款入参（api-doc 10.1）
 */
@Data
public class RefundCreateDTO {

    @NotNull(message = "订单明细 ID 不能为空")
    private Long orderItemId;

    /** 1=仅退款，2=退款+退货 */
    @NotNull(message = "退款类型不能为空")
    @Min(value = 1, message = "退款类型取值非法")
    @Max(value = 2, message = "退款类型取值非法")
    private Integer type;

    @NotBlank(message = "退款原因不能为空")
    @Size(max = 500, message = "退款原因最长 500 字")
    private String reason;

    /** 退款金额（分） */
    @NotNull(message = "退款金额不能为空")
    private Integer amount;
}
