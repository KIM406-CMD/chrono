package com.ncist.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建/编辑秒杀券请求。
 *
 * <p>管理员配置秒杀券活动时使用；时间格式为 yyyy-MM-dd HH:mm:ss。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillCouponDTO {

    /** 券名称，必填。 */
    @NotBlank(message = "券名称不能为空")
    private String name;

    /** 券描述，可选。 */
    private String description;

    /** 优惠类型，必填：1=满减券，2=折扣券。 */
    @NotNull(message = "优惠类型不能为空")
    private Integer discountType;

    /** 优惠值，必填且大于 0；满减券为减免金额（分），折扣券为折扣百分比（如 85 表示 8.5 折）。 */
    @NotNull(message = "优惠值不能为空")
    @Min(value = 1, message = "优惠值必须大于0")
    private Integer discountValue;

    /** 最低消费金额，单位：分，必填；0 表示无门槛。 */
    @NotNull(message = "最低消费金额不能为空")
    @Min(value = 0, message = "最低消费金额不能为负数")
    private Integer minAmount;

    /** 发放总量，必填且必须大于 0。 */
    @NotNull(message = "发放总量不能为空")
    @Min(value = 1, message = "发放总量必须大于0")
    private Integer totalCount;

    /** 活动开始时间，必填，格式 yyyy-MM-dd HH:mm:ss。 */
    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    /** 活动结束时间，必填，格式 yyyy-MM-dd HH:mm:ss。 */
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    /** 每人限领数量，可选；填写时必须大于 0。 */
    @Min(value = 1, message = "每人限领数量必须大于0")
    private Integer perUserLimit;
}
