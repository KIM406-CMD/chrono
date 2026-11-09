package com.ncist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消订单请求。
 *
 * <p>商城端用户取消待支付订单时使用；取消成功后恢复库存并记录取消原因。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderDTO {

    /** 取消原因，可选；为空时仅记录订单状态变更。 */
    private String reason;
}
