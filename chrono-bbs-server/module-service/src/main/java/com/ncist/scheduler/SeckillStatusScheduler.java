package com.ncist.scheduler;

import com.ncist.mapper.SeckillCouponMapper;
import com.ncist.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 秒杀券状态定时刷新：
 * 未开始 → 进行中（start_time 到点）、进行中 → 已结束（end_time 过期）、
 * 未使用的用户券随券结束自动过期
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillStatusScheduler {

    private final SeckillCouponMapper seckillCouponMapper;
    private final UserCouponMapper userCouponMapper;

    // 每 30 秒刷新一次
    @Scheduled(fixedDelay = 30_000)
    public void refreshSeckillStatus() {
        try {
            int activated = seckillCouponMapper.activateStarted();
            int finished = seckillCouponMapper.finishEnded();
            int expired = userCouponMapper.expireEnded();
            if (activated > 0 || finished > 0 || expired > 0) {
                log.info("秒杀状态刷新：{} 张券开始，{} 张券结束，{} 张用户券过期", activated, finished, expired);
            }
        } catch (Exception e) {
            log.warn("秒杀状态刷新失败: {}", e.getMessage());
        }
    }
}
