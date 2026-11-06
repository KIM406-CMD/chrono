package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家视角的订单列表/详情 VO
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
    private Integer totalAmount;
    private Integer status;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime createdAt;
    private Integer itemCount;
    private List<ItemSnapshot> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemSnapshot {
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private Integer unitPrice;
        private Integer subtotal;
    }
}
