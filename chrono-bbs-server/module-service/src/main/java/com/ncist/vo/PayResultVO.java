package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 支付结果视图（对齐前端卡号支付返回）。
 *
 * <p>卡号支付完成后返回订单最新状态与支付时间。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayResultVO {

    /** 支付状态：0=处理中，1=成功，2=失败。 */
    private Integer status;
    /** 支付成功时间，支付失败时为空。 */
    private LocalDateTime paidAt;
}
