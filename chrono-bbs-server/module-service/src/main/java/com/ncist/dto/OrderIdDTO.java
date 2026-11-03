package com.ncist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单 ID 请求体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderIdDTO {

    @NotNull(message = "订单 ID 不能为空")
    private Long orderId;
}
