package com.ncist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消订单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderDTO {

    private String reason;
}
