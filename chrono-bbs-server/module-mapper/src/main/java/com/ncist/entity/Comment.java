package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 商品评论实体，对应 {@code comment} 表。
 *
 * <p>通过 {@code parentId} 自关联实现多级嵌套回复；{@code isAppend} 用于区分首次评价与追评。
 * {@code status} 为 1 时表示已被管理员或商家逻辑删除（查询时过滤）。</p>
 */
@Builder
public class Comment {

    private Long id;
    /** 被评论的商品 ID，关联 product 表。 */
    private Long productId;
    /** 评论人用户 ID，关联 user 表。 */
    private Long userId;
    /** 关联的订单明细 ID；购买后评价时有值，普通回复可为空。 */
    private Long orderItemId;
    /** 父评论 ID：null 表示一级评论，非 null 表示回复。 */
    private Long parentId;
    /** 评论内容，最长 1000 字符。 */
    private String content;
    /** 星级评分 1-5，仅一级评论有值；回复为 null。 */
    private Integer star;
    /** 是否追评：0-首次评价，1-追评。 */
    private Integer isAppend;
    /** 状态：0-正常，1-已删除（管理员/商家删除）。 */
    private Integer status;
    /** 评论时间。 */
    private LocalDateTime createdAt;
}