package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 管理后台-全局销售报表。
 *
 * <p>管理员查看全平台销售概览、热销商品、分类/商家销售分布与趋势时使用。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReportVO {

    /** 总销售额，单位：分。 */
    private Long totalSales;
    /** 总订单数。 */
    private Long totalOrders;
    /** 平均订单金额，单位：分。 */
    private Long avgOrderAmount;
    /** 热销商品排行。 */
    private List<ProductSales> topProducts;
    /** 按分类统计的销售额。 */
    private List<CategorySales> salesByCategory;
    /** 按商家统计的销售额。 */
    private List<MerchantSales> salesByMerchant;
    /** 销售趋势（按日期聚合）。 */
    private List<TrendPoint> salesTrend;
    /** 订单量趋势（按日聚合下单/支付/完成量）。 */
    private List<OrderTrendPoint> orderTrend;
    /** 订单状态分布：pending/paid/shipped/completed/cancelled */
    private Map<String, Long> orderStatusDistribution;

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
     * 商家销售统计项。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MerchantSales {
        private Long merchantId;
        private String shopName;
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

    /**
     * 订单量趋势数据点。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderTrendPoint {
        /** 日期，格式 yyyy-MM-dd。 */
        private String date;
        /** 当日下单量。 */
        private Long createdCount;
        /** 当日支付量。 */
        private Long paidCount;
        /** 当日完成量。 */
        private Long completedCount;
    }
}
