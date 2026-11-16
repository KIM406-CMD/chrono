package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.vo.NotificationVO;

/**
 * 站内通知服务。
 *
 * <p>面向买家提供通知列表、未读数与已读操作；同时为订单、支付、发货等
 * 业务节点提供通知投递能力。通知只允许接收人本人读取与标记已读。</p>
 */
public interface NotificationService {

    /** 通知类型：订单通知。 */
    int TYPE_ORDER = 1;
    /** 通知类型：系统通知。 */
    int TYPE_SYSTEM = 2;
    /** 通知类型：售后通知。 */
    int TYPE_AFTER_SALE = 3;

    /** 关联对象：订单。 */
    int RELATED_ORDER = 1;
    /** 关联对象：退款。 */
    int RELATED_REFUND = 2;

    /**
     * 分页查询当前用户的站内通知。
     *
     * @param userId     当前用户 ID
     * @param unreadOnly 是否仅查未读；{@code null} 或 {@code false} 表示查全部
     * @param page       页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size       每页条数；为空或小于 1 时按 10 处理
     * @return 通知分页结果
     */
    PageResult<NotificationVO> list(Long userId, Boolean unreadOnly, Integer page, Integer size);

    /**
     * 查询当前用户的未读通知数量。
     *
     * @param userId 当前用户 ID
     * @return 未读通知数
     */
    long unreadCount(Long userId);

    /**
     * 将指定通知标记为已读。
     *
     * @param userId 当前用户 ID
     * @param id     通知 ID
     * @throws com.ncist.common.exception.BusinessException 通知不存在或不属于当前用户时抛出
     */
    void markRead(Long userId, Long id);

    /**
     * 将当前用户全部未读通知标记为已读。
     *
     * @param userId 当前用户 ID
     * @return 本次标记为已读的通知数量
     */
    int markAllRead(Long userId);

    /**
     * 下单成功通知。
     *
     * @param userId      买家用户 ID
     * @param orderId     订单 ID
     * @param orderNo     订单编号
     * @param totalAmount 订单金额（分）
     */
    void notifyOrderCreated(Long userId, Long orderId, String orderNo, long totalAmount);

    /**
     * 支付成功通知。
     *
     * @param userId      买家用户 ID
     * @param orderId     订单 ID
     * @param orderNo     订单编号
     * @param totalAmount 支付金额（分）
     */
    void notifyOrderPaid(Long userId, Long orderId, String orderNo, long totalAmount);

    /**
     * 订单发货通知。
     *
     * @param userId  买家用户 ID
     * @param orderId 订单 ID
     * @param orderNo 订单编号
     */
    void notifyOrderShipped(Long userId, Long orderId, String orderNo);
}
