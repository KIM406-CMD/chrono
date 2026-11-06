package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 追加评论入参（api-doc 9.3）
 */
@Data
public class CommentAppendDTO {

    @NotNull(message = "评论 ID 不能为空")
    private Long commentId;

    @NotBlank(message = "追评内容不能为空")
    @Size(max = 1000, message = "追评内容最长 1000 字")
    private String content;
}
