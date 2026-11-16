package com.ncist.service.impl;

import com.ncist.common.enums.CommonErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.entity.Merchant;
import com.ncist.entity.Notification;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.NotificationMapper;
import com.ncist.service.NotificationService;
import com.ncist.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 站内通知服务实现。
 *
 * <p>通知记录只写入 {@code notification} 表，不占用聊天会话；
 * 列表查询按创建时间倒序分页，已读操作以“通知 ID + 接收用户”为条件，
 * 保证用户无法操作他人通知。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    /** 通知数据访问组件。 */
    private final NotificationMapper notificationMapper;
    /** 商家数据访问组件，用于按 merchant_id 定位商家用户。 */
    private final MerchantMapper merchantMapper;

    /** {@inheritDoc} */
    @Override
    public PageResult<NotificationVO> list(Long userId, Boolean unreadOnly, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 100);
        boolean onlyUnread = Boolean.TRUE.equals(unreadOnly);
        long total = notificationMapper.countByUser(userId, onlyUnread);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<NotificationVO> list = notificationMapper
                .selectPage(userId, onlyUnread, (p - 1) * s, s).stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(total, p, s, list);
    }

    /** {@inheritDoc} */
    @Override
    public long unreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    /** {@inheritDoc} */
    @Override
    public void markRead(Long userId, Long id) {
        if (notificationMapper.markRead(id, userId) == 0) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int markAllRead(Long userId) {
        return notificationMapper.markAllRead(userId);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyOrderCreated(Long userId, Long orderId, String orderNo, long totalAmount) {
        create(userId, TYPE_ORDER, "订单提交成功",
                "订单 " + orderNo + " 已提交，应付金额 ¥" + yuan(totalAmount)
                        + "，请尽快完成支付。",
                RELATED_ORDER, orderId);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyOrderPaid(Long userId, Long orderId, String orderNo, long totalAmount) {
        create(userId, TYPE_ORDER, "订单支付成功",
                "订单 " + orderNo + " 已支付 ¥" + yuan(totalAmount) + "，等待商家发货。",
                RELATED_ORDER, orderId);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyOrderShipped(Long userId, Long orderId, String orderNo) {
        create(userId, TYPE_ORDER, "订单已发货",
                "订单 " + orderNo + " 已发货，请留意物流信息并及时确认收货。",
                RELATED_ORDER, orderId);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyOrderPaidToMerchants(Long orderId, String orderNo) {
        // 一个订单可能包含多个商家的商品，按商家分别推送，只包含该商家的商品小计
        for (var row : merchantMapper.selectOrderMerchants(orderId)) {
            Long merchantUserId = toLong(row.get("userId"));
            if (merchantUserId == null) {
                continue;
            }
            Long amount = toLong(row.get("amount"));
            create(merchantUserId, TYPE_ORDER, "新订单待发货",
                    "订单 " + orderNo + " 已支付，您的商品金额 ¥" + yuan(amount == null ? 0L : amount)
                            + "，请及时发货。",
                    RELATED_ORDER, orderId);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notifyRefundApplied(Long merchantId, Long refundId, Long orderId,
                                    String productName, long amount) {
        Long merchantUserId = merchantUserId(merchantId);
        if (merchantUserId == null) {
            return;
        }
        String name = productName == null || productName.isBlank() ? "退款商品" : productName;
        create(merchantUserId, TYPE_AFTER_SALE, "新的退款申请",
                "商品「" + name + "」收到退款/退货申请，金额 ¥" + yuan(amount)
                        + "，请及时审核。",
                RELATED_REFUND, refundId);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyRefundReviewed(Long userId, Long refundId, Long orderId,
                                     boolean approved, String reason, boolean isReturn) {
        String title;
        String content;
        if (approved) {
            if (isReturn) {
                title = "退货申请已通过";
                content = "您的退货申请已通过，请填写退货物流信息并寄回商品。";
            } else {
                title = "退款申请已通过";
                content = "您的退款申请已通过，退款将按支付渠道原路退回。";
            }
        } else {
            title = "退款申请被拒绝";
            content = "您的退款申请被拒绝"
                    + (reason == null || reason.isBlank() ? "。" : "，原因：" + reason + "。");
        }
        create(userId, TYPE_AFTER_SALE, title, content, RELATED_REFUND, refundId);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyReturnSubmitted(Long merchantId, Long refundId, Long orderId, String productName) {
        Long merchantUserId = merchantUserId(merchantId);
        if (merchantUserId == null) {
            return;
        }
        String name = productName == null || productName.isBlank() ? "退货商品" : productName;
        create(merchantUserId, TYPE_AFTER_SALE, "买家已寄回商品",
                "商品「" + name + "」的退货商品已寄回，请收到后确认退款。",
                RELATED_REFUND, refundId);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyRefundCompleted(Long userId, Long refundId, Long orderId) {
        create(userId, TYPE_AFTER_SALE, "退款已完成",
                "您的退货已确认收货，退款已原路退回。",
                RELATED_REFUND, refundId);
    }

    /**
     * 按商家 ID 查询商家用户 ID。
     *
     * @param merchantId 商家 ID（merchant 表主键）
     * @return 商家用户 ID；商家不存在时返回 null
     */
    private Long merchantUserId(Long merchantId) {
        if (merchantId == null) {
            return null;
        }
        Merchant merchant = merchantMapper.selectById(merchantId);
        return merchant == null ? null : merchant.getUserId();
    }

    /**
     * 将 Map 中的数值转换为 Long，兼容 Integer/Long/BigDecimal。
     *
     * @param value 原始值
     * @return 转换后的 Long；无法转换时返回 0
     */
    private Long toLong(Object value) {
        if (value instanceof Number num) {
            return num.longValue();
        }
        return value == null ? null : Long.parseLong(String.valueOf(value));
    }

    /**
     * 写入一条通知记录。
     *
     * @param userId      接收用户 ID
     * @param type        通知类型
     * @param title       通知标题
     * @param content     通知正文
     * @param relatedType 关联对象类型
     * @param relatedId   关联对象 ID
     */
    private void create(Long userId, int type, String title, String content,
                        int relatedType, Long relatedId) {
        try {
            notificationMapper.insert(Notification.builder()
                    .userId(userId)
                    .type(type)
                    .title(title)
                    .content(content)
                    .relatedType(relatedType)
                    .relatedId(relatedId)
                    .isRead(0)
                    .build());
        } catch (Exception e) {
            // 通知属于辅助能力，写入失败只记录日志，不影响下单/支付/发货主流程
            log.warn("站内通知写入失败: userId={}, title={}, error={}", userId, title, e.getMessage());
        }
    }

    /**
     * 将金额（分）格式化为两位小数的元。
     *
     * @param fen 金额，单位：分
     * @return 元字符串，如 {@code 39.90}
     */
    private String yuan(long fen) {
        return BigDecimal.valueOf(fen).movePointLeft(2).toPlainString();
    }

    /**
     * 将通知实体转换为视图对象。
     *
     * @param notification 通知实体
     * @return 通知视图对象
     */
    private NotificationVO toVO(Notification notification) {
        return NotificationVO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .relatedType(notification.getRelatedType())
                .relatedId(notification.getRelatedId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
