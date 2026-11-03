package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.CreateOrderDTO;
import com.ncist.vo.CreateOrderVO;
import com.ncist.vo.OrderDetailVO;
import com.ncist.vo.OrderListItemVO;
import com.ncist.vo.SubmitTokenVO;

/**
 * 订单服务
 */
public interface OrderService {

    // 获取防重复提交 Token（Redis，5 分钟有效，使用后立即删除）
    SubmitTokenVO getSubmitToken(Long userId);

    // 创建订单：校验 Token/地址/购物车项 → 原子锁库存 → 用券抵扣 → 写订单与明细 → 清理购物车 → 核销券
    CreateOrderVO createOrder(Long userId, CreateOrderDTO dto);

    // 我的订单分页（status 不传查全部）
    PageResult<OrderListItemVO> listOrders(Long userId, Integer status, Integer page, Integer size);

    // 订单详情（含订单项）
    OrderDetailVO getOrderDetail(Long userId, Long id);

    // 取消订单（仅待支付）：回补库存 + 回退优惠券
    void cancelOrder(Long userId, Long id, String reason);

    // 确认收货（仅已发货）
    void receiveOrder(Long userId, Long id);

    // 删除订单（仅已完成/已取消）
    void deleteOrder(Long userId, Long id);
}
