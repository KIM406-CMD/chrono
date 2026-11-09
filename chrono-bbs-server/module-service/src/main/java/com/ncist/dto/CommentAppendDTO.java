package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 追加评论入参（api-doc 9.3）。
 *
 * <p>用户对已发表的一级评论补充追评时使用；每条一级评论仅允许追评一次。</p>
 */
@Data
public class CommentAppendDTO {

    /** 被追评的一级评论 ID，必填，必须属于当前登录用户。 */
    @NotNull(message = "评论 ID 不能为空")
    private Long commentId;

    /** 追评内容，必填，最长 1000 字。 */
    @NotBlank(message = "追评内容不能为空")
    @Size(max = 1000, message = "追评内容最长 1000 字")
    private String content;
}
