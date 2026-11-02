package com.ncist.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 秒杀券列表/详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillCouponVO {

    private Long id;
    private String name;
    private String description;
    private Integer discountType;
    private Integer discountValue;
    private Integer minAmount;
    private Integer totalCount;
    private Integer remainCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer perUserLimit;
    private Integer status;
}
