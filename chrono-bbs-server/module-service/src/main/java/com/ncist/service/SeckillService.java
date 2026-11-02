package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.SeckillCalculateDTO;
import com.ncist.dto.SeckillCouponDTO;
import com.ncist.vo.MyCouponVO;
import com.ncist.vo.SeckillCalculateVO;
import com.ncist.vo.SeckillClaimVO;
import com.ncist.vo.SeckillCouponVO;

/**
 * 秒杀券服务
 */
public interface SeckillService {

    // 秒杀券列表（游客可访问）：status 不传查全部可抢券（未开始+进行中），1-进行中，0-未开始
    PageResult<SeckillCouponVO> listCoupons(Integer status, Integer page, Integer size);

    // 秒杀券详情（游客可访问）
    SeckillCouponVO getCouponDetail(Long id);

    // 抢领秒杀券：Redis DECR 防超卖 + per_user_limit 防超领，Redis 故障降级数据库原子扣减
    SeckillClaimVO claimCoupon(Long couponId, Long userId);

    // 我的优惠券：status 0-未使用，1-已使用，2-已过期，不传查全部
    PageResult<MyCouponVO> listMyCoupons(Long userId, Integer status, Integer page, Integer size);

    // 创建秒杀券（商家/管理员），remain_count = total_count
    Long createCoupon(SeckillCouponDTO dto, Integer role);

    // 编辑秒杀券（商家/管理员）：totalCount 变化量自动同步 remain_count
    void updateCoupon(Long id, SeckillCouponDTO dto, Integer role);

    // 删除秒杀券（商家/管理员）：仅未开始的券可删除
    void deleteCoupon(Long id, Integer role);

    // 下单时计算优惠：userCouponId 不传则不使用券；校验未使用、未过期、满足最低消费
    SeckillCalculateVO calculate(SeckillCalculateDTO dto, Long userId);
}
