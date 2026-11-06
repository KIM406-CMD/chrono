package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantStatsVO {

    private Long totalSales;
    private Long orderCount;
    private List<ProductSales> topProducts;
    private List<CategorySales> salesByCategory;
    private List<TrendPoint> salesTrend;

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
    public static class TrendPoint {
        private String date;
        private Long amount;
    }
}
