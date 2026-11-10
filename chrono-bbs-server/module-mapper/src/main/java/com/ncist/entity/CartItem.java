package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 购物车项实体，对应 {@code cart_item} 表。
 *
 * <p>同一用户与同一 SKU 只允许存在一条记录（唯一索引 uk_user_sku），
 * 重复加购时由业务层合并数量而非新增记录。</p>
 */
@Builder
public class CartItem {

    private Long id;
    /** 所属用户 ID，关联 user 表。 */
    private Long userId;
    /** 商品 ID，关联 product 表，便于按商品维度统计。 */
    private Long productId;
    /** SKU ID，关联 product_sku 表，决定价格与库存。 */
    private Long skuId;
    /** 购买数量，必须大于 0。 */
    private Integer quantity;
    /** 是否选中参与结算：0-未选中，1-已选中。 */
    private Integer selected;
    /** 加入购物车时间。 */
    private LocalDateTime createdAt;
}
