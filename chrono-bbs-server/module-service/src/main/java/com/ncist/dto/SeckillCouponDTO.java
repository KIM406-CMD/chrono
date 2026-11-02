package com.ncist.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建/编辑秒杀券请求（时间格式 yyyy-MM-dd HH:mm:ss）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillCouponDTO {

    @NotBlank(message = "券名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "优惠类型不能为空")
    private Integer discountType;

    @NotNull(message = "优惠值不能为空")
    @Min(value = 1, message = "优惠值必须大于0")
    private Integer discountValue;

    @NotNull(message = "最低消费金额不能为空")
    @Min(value = 0, message = "最低消费金额不能为负数")
    private Integer minAmount;

    @NotNull(message = "发放总量不能为空")
    @Min(value = 1, message = "发放总量必须大于0")
    private Integer totalCount;

    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    @Min(value = 1, message = "每人限领数量必须大于0")
    private Integer perUserLimit;
}
