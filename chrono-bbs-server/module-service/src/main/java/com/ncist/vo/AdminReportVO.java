package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 管理后台-全局销售报表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReportVO {

    private Long totalSales;
    private Long totalOrders;
    private Long avgOrderAmount;
    private List<ProductSales> topProducts;
    private List<CategorySales> salesByCategory;
    private List<MerchantSales> salesByMerchant;
    private List<TrendPoint> salesTrend;
    /** 订单状态分布：pending/paid/shipped/completed/cancelled */
    private Map<String, Long> orderStatusDistribution;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSales {
        private Long productId;
        private String productName;
        private Long salesCount;
        private Long totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySales {
        private Long categoryId;
        private String categoryName;
        private Long totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MerchantSales {
        private Long merchantId;
        private String shopName;
        private Long totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        private String date;
        private Long amount;
    }
}
