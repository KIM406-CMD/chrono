package com.ncist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论入参（api-doc 9.2）
 */
@Data
public class CommentCreateDTO {

    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    @NotNull(message = "订单明细 ID 不能为空")
    private Long orderItemId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容最长 1000 字")
    private String content;

    @NotNull(message = "星级不能为空")
    @Min(value = 1, message = "星级取值 1-5")
    @Max(value = 5, message = "星级取值 1-5")
    private Integer star;
}
