package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeckillCoupon {

    private Long id;
    private String name;
    private String description;
    private Integer discountType;
    private Integer discountValue;
    private Integer minAmount;
    private Integer totalCount;
    private Integer remainCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer perUserLimit;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
