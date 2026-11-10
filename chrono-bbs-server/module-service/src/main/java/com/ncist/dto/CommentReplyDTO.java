package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家回复入参（api-doc 9.4）。
 *
 * <p>商家对所属商品的用户评价进行公开回复时使用。</p>
 */
@Data
public class CommentReplyDTO {

    /** 被回复的评论 ID，必填；评论所属商品必须由当前商家经营。 */
    @NotNull(message = "被回复的评论 ID 不能为空")
    private Long parentId;

    /** 回复内容，必填，最长 1000 字。 */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容最长 1000 字")
    private String content;
}
