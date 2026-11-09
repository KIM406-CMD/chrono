package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商家销售统计报表。
 *
 * <p>商家后台查看本店铺销售额、订单量、热销商品与销售趋势时使用。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantStatsVO {

    /** 总销售额，单位：分。 */
    private Long totalSales;
    /** 订单总数。 */
    private Long orderCount;
    /** 热销商品排行。 */
    private List<ProductSales> topProducts;
    /** 按分类统计的销售额。 */
    private List<CategorySales> salesByCategory;
    /** 销售趋势（按日期聚合）。 */
    private List<TrendPoint> salesTrend;

    /**
     * 热销商品统计项。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSales {
        private Long productId;
        private String productName;
        /** 销量（件数）。 */
        private Long salesCount;
        /** 销售额，单位：分。 */
        private Long totalAmount;
    }

    /**
     * 分类销售统计项。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySales {
        private Long categoryId;
        private String categoryName;
        /** 销售额，单位：分。 */
        private Long totalAmount;
    }

    /**
     * 销售趋势数据点。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        /** 日期，格式 yyyy-MM-dd。 */
        private String date;
        /** 当日销售额，单位：分。 */
        private Long amount;
    }
}
