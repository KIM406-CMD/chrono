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
 * 商品基础信息实体，对应 {@code product} 表。
 *
 * <p>只存描述性字段，价格与库存下沉到 {@link ProductSku}；{@code sales} 使用 Redis
 * 累加后定时同步回数据库，避免高频写库。</p>
 */
@Builder
public class Product {

    private Long id;
    /** 商品名称，最长 200 字符。 */
    private String name;
    /** 商品描述（富文本）。 */
    private String description;
    /** 商品图片 URL 数组（JSON 字符串，最多 4 张）。 */
    private String images;
    /** 品牌名称。 */
    private String brand;
    /** 所属三级分类 ID，关联 category 表。 */
    private Long categoryId;
    /** 所属商家 ID，关联 merchant 表主键。 */
    private Long merchantId;
    /** 上下架状态：0-下架，1-上架；仅上架商品对游客可见。 */
    private Integer status;
    /** 累计销量，由 Redis 计数定时同步。 */
    private Integer sales;
    /** 创建/上架时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}