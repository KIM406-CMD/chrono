package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.CreateOrderDTO;
import com.ncist.vo.CreateOrderVO;
import com.ncist.vo.OrderDetailVO;
import com.ncist.vo.OrderListItemVO;
import com.ncist.vo.SubmitTokenVO;

/**
 * 订单服务。
 *
 * <p>负责下单、订单查询、取消、确认收货与删除等全生命周期操作。
 * 下单流程通过 Redis 防重复提交 Token 与数据库原子扣减库存保证幂等与并发安全，
 * 取消订单时回补库存并回退优惠券；涉及多表写操作的创建、取消与删除
 * 由实现类以事务保证一致性。</p>
 */
public interface OrderService {

    /**
     * 获取防重复提交 Token。
     *
     * <p>Token 写入 Redis 并绑定当前用户，5 分钟内有效，下单成功后立即删除，
     * 保证同一 Token 只能提交一次订单。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @return 防重复提交 Token
     */
    SubmitTokenVO getSubmitToken(Long userId);

    /**
     * 创建订单。
     *
     * <p>依次校验并消费提交 Token、校验收货地址属主、校验购物车项上架与库存，
     * 随后原子扣减 SKU 库存、计算优惠券抵扣、写入订单与明细快照、
     * 清理已下单购物车项并核销优惠券。任一步骤失败整体回滚。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    下单参数，包含提交 Token、地址、购物车项与可选用户券
     * @return 创建结果，包含订单 ID、订单号、实付金额与状态
     * @throws BusinessException 提交 Token 无效/已使用、地址不存在或不属于当前用户、
     *                           购物车项为空、商品下架、库存不足或优惠券不可用时抛出
     */
    CreateOrderVO createOrder(Long userId, CreateOrderDTO dto);

    /**
     * 分页查询当前用户的订单。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param status 订单状态过滤，为空时查询全部状态
     * @param page   页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size   每页条数；为空或小于 1 时按 10 处理
     * @return 订单分页结果，包含订单项数量与首项概要信息
     */
    PageResult<OrderListItemVO> listOrders(Long userId, Integer status, Integer page, Integer size);

    /**
     * 查询订单详情（含订单项）。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param id     订单 ID
     * @return 订单详情
     * @throws BusinessException 订单不存在或不属于当前用户时抛出
     */
    OrderDetailVO getOrderDetail(Long userId, Long id);

    /**
     * 取消订单（仅待支付状态）。
     *
     * <p>取消成功后回补各订单项库存，并将已使用的优惠券恢复为未使用。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param id     订单 ID
     * @param reason 取消原因
     * @throws BusinessException 订单不存在、不属于当前用户或当前状态不允许取消时抛出
     */
    void cancelOrder(Long userId, Long id, String reason);

    /**
     * 确认收货（仅已发货状态）。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param id     订单 ID
     * @throws BusinessException 订单不存在、不属于当前用户或当前状态不允许确认收货时抛出
     */
    void receiveOrder(Long userId, Long id);

    /**
     * 删除订单（仅已完成或已取消状态），同时删除订单明细。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param id     订单 ID
     * @throws BusinessException 订单不存在、不属于当前用户或当前状态不允许删除时抛出
     */
    void deleteOrder(Long userId, Long id);
}
