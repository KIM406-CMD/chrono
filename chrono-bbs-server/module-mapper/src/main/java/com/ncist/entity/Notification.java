package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 站内通知实体，对应 {@code notification} 表。
 *
 * <p>用于向买家推送订单、支付、发货与售后等系统消息，
 * 与聊天消息分开存储，避免抢占商家会话。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private Long id;
    /** 接收用户 ID，关联 user 表。 */
    private Long userId;
    /** 通知类型：1-订单通知，2-系统通知，3-售后通知。 */
    private Integer type;
    /** 通知标题，如“订单提交成功”。 */
    private String title;
    /** 通知正文，展示在通知中心。 */
    private String content;
    /** 关联对象类型：1-订单，2-退款；可为空。 */
    private Integer relatedType;
    /** 关联对象 ID，如订单 ID，用于点击跳转。 */
    private Long relatedId;
    /** 阅读状态：0-未读，1-已读。 */
    private Integer isRead;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
