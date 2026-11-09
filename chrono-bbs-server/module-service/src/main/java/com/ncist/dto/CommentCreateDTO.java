package com.ncist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论入参（api-doc 9.2）。
 *
 * <p>用户确认收货后对订单明细中的商品发表评价时使用；服务端校验订单归属与状态。</p>
 */
@Data
public class CommentCreateDTO {

    /** 被评价商品 ID，必填，需与订单明细中的商品一致。 */
    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    /** 已完成订单的明细 ID，必填；用于校验当前用户确实购买过该商品。 */
    @NotNull(message = "订单明细 ID 不能为空")
    private Long orderItemId;

    /** 评论内容，必填，最长 1000 字。 */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容最长 1000 字")
    private String content;

    /** 星级评分，必填，取值 1-5。 */
    @NotNull(message = "星级不能为空")
    @Min(value = 1, message = "星级取值 1-5")
    @Max(value = 5, message = "星级取值 1-5")
    private Integer star;
}
