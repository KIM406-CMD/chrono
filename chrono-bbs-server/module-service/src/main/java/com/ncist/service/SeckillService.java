package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.SeckillCalculateDTO;
import com.ncist.dto.SeckillCouponDTO;
import com.ncist.vo.MyCouponVO;
import com.ncist.vo.SeckillCalculateVO;
import com.ncist.vo.SeckillClaimVO;
import com.ncist.vo.SeckillCouponVO;

/**
 * 秒杀券服务。
 *
 * <p>提供秒杀券的浏览、抢领、我的优惠券查询、后台管理以及下单优惠计算。
 * 抢领通过 Redis 原子扣减库存与领取计数实现防超卖、防超领，
 * Redis 不可用时降级为数据库原子扣减；数据库写入失败时对 Redis 做尽力而为的补偿。
 * 优惠券创建、编辑、删除仅允许商家或管理员操作。</p>
 */
public interface SeckillService {

    /**
     * 分页查询秒杀券列表（游客可访问）。
     *
     * @param status 状态过滤：0-未开始，1-进行中；为空时查询全部可抢券（未开始与进行中）
     * @param page   页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size   每页条数；为空或小于 1 时按 10 处理，最大 100
     * @param userId 当前登录用户 ID，可为空（游客）；非空时回填用户领取状态
     * @return 秒杀券分页结果
     * @throws BusinessException status 非法时抛出
     */
    PageResult<SeckillCouponVO> listCoupons(Integer status, Integer page, Integer size, Long userId);

    /**
     * 查询秒杀券详情（游客可访问）。
     *
     * @param id     秒杀券 ID
     * @param userId 当前登录用户 ID，可为空（游客）；非空时回填用户领取状态
     * @return 秒杀券详情
     * @throws BusinessException 秒杀券不存在时抛出
     */
    SeckillCouponVO getCouponDetail(Long id, Long userId);

    /**
     * 抢领秒杀券。
     *
     * <p>要求活动进行中；先通过 Redis DECR 剩余库存与 INCR 用户领取计数实现限流，
     * 超过单人限领或库存耗尽时回补并拒绝；Redis 不可用时降级为数据库原子扣减。
     * 数据库写入失败时事务回滚并补偿 Redis。</p>
     *
     * @param couponId 秒杀券 ID
     * @param userId   用户 ID，必须为当前登录用户
     * @return 领券结果，包含用户券 ID 与券信息
     * @throws BusinessException 秒杀券不存在、活动未进行、已抢光或超过限领数量时抛出
     */
    SeckillClaimVO claimCoupon(Long couponId, Long userId);

    /**
     * 分页查询当前用户的优惠券。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param status 状态过滤：0-未使用，1-已使用，2-已过期；为空时查询全部
     * @param page   页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size   每页条数；为空或小于 1 时按 10 处理，最大 100
     * @return 我的优惠券分页结果
     * @throws BusinessException status 非法时抛出
     */
    PageResult<MyCouponVO> listMyCoupons(Long userId, Integer status, Integer page, Integer size);

    /**
     * 创建秒杀券，剩余数量初始化为总数量。
     *
     * @param dto  秒杀券信息，包含名称、优惠类型/值、总量、时间窗与限领数
     * @param role 当前用户角色，必须为商家或管理员
     * @return 新增秒杀券 ID
     * @throws BusinessException 无管理权限、时间格式/范围非法或优惠参数非法时抛出
     */
    Long createCoupon(SeckillCouponDTO dto, Integer role);

    /**
     * 编辑秒杀券。
     *
     * <p>总量变化时按差值同步剩余数量，若 Redis 库存键存在也同步增减；
     * 时间窗变更后重新推导券状态。</p>
     *
     * @param id   秒杀券 ID
     * @param dto  秒杀券信息
     * @param role 当前用户角色，必须为商家或管理员
     * @throws BusinessException 无管理权限、秒杀券不存在或时间/优惠参数非法时抛出
     */
    void updateCoupon(Long id, SeckillCouponDTO dto, Integer role);

    /**
     * 删除秒杀券，仅未开始的券允许删除。
     *
     * @param id   秒杀券 ID
     * @param role 当前用户角色，必须为商家或管理员
     * @throws BusinessException 无管理权限、秒杀券不存在或券已开始/结束时抛出
     */
    void deleteCoupon(Long id, Integer role);

    /**
     * 下单时计算优惠金额。
     *
     * <p>未传用户券 ID 时按原价计算；传入时校验用户券存在且属于当前用户、
     * 状态为未使用、券未过期并满足最低消费门槛。</p>
     *
     * @param dto    计算参数，包含订单总金额与可选用户券 ID
     * @param userId 用户 ID，必须为当前登录用户
     * @return 优惠计算结果，包含原价、优惠金额、实付金额与券名称
     * @throws BusinessException 用户券不存在/不属于当前用户、已使用/已过期
     *                           或未达到最低消费时抛出
     */
    SeckillCalculateVO calculate(SeckillCalculateDTO dto, Long userId);
}