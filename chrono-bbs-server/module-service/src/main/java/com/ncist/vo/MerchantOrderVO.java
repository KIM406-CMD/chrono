package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家视角的订单列表/详情 VO。
 *
 * <p>商家后台查看自己店铺相关订单时使用；仅包含与商家相关的订单项。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantOrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    /** 订单金额，单位：分。 */
    private Integer totalAmount;
    /** 订单状态码：1=待支付，2=已支付，3=已发货，4=已完成，5=已取消，6=退款中，7=已退款。 */
    private Integer status;
    /** 支付时间，未支付时为空。 */
    private LocalDateTime paidAt;
    /** 发货时间，未发货时为空。 */
    private LocalDateTime shippedAt;
    private LocalDateTime createdAt;
    /** 订单项数量。 */
    private Integer itemCount;
    /** 订单项快照列表（仅当前商家的商品）。 */
    private List<ItemSnapshot> items;

    /**
     * 商家订单中的商品快照项。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemSnapshot {
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        /** 下单时单价，单位：分。 */
        private Integer unitPrice;
        /** 小计，单位：分。 */
        private Integer subtotal;
    }
}
