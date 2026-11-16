package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 站内通知视图对象。
 *
 * <p>返回给通知中心列表，包含标题、正文、已读状态与关联业务对象，
 * 前端可依据 {@code relatedType / relatedId} 跳转到订单或退款详情。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {

    /** 通知 ID。 */
    private Long id;
    /** 通知类型：1-订单通知，2-系统通知，3-售后通知。 */
    private Integer type;
    /** 通知标题。 */
    private String title;
    /** 通知正文。 */
    private String content;
    /** 关联对象类型：1-订单，2-退款。 */
    private Integer relatedType;
    /** 关联对象 ID。 */
    private Long relatedId;
    /** 阅读状态：0-未读，1-已读。 */
    private Integer isRead;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
