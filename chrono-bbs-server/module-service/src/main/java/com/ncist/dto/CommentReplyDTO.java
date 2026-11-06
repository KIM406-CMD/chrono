package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家回复入参（api-doc 9.4）
 */
@Data
public class CommentReplyDTO {

    @NotNull(message = "被回复的评论 ID 不能为空")
    private Long parentId;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容最长 1000 字")
    private String content;
}
