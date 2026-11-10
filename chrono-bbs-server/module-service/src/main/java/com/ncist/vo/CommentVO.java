package com.ncist.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论出参（api-doc 9.1）。
 *
 * <p>商品详情页展示评价列表时使用；一级评论可携带追评与商家回复。</p>
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
    /** 评论图片 URL 列表。 */
    private List<String> images;
    /** 星级评分，取值 1-5；追评、商家回复等非一级评论可能为空。 */
    private Integer star;
    private LocalDateTime createdAt;

    /** 追评内容（仅一级评论可能有） */
    private CommentVO append;

    /** 商家回复列表 */
    private List<CommentVO> replies;
}
