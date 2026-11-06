package com.ncist.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论出参（api-doc 9.1）
 */
@Data
@Builder
public class CommentVO {

    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long orderItemId;
    private String content;
    private Integer star;
    private LocalDateTime createdAt;

    /** 追评内容（仅一级评论可能有） */
    private CommentVO append;

    /** 商家回复列表 */
    private List<CommentVO> replies;
}
