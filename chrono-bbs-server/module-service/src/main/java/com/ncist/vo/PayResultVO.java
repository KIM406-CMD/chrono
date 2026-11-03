package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 支付结果视图（对齐前端卡号支付返回）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayResultVO {

    private Integer status;
    private LocalDateTime paidAt;
}
