package com.ncist.scheduler;

import com.ncist.mapper.SeckillCouponMapper;
import com.ncist.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 秒杀券状态定时刷新任务。
 *
 * <p>定时推进秒杀券状态：未开始 → 进行中（到达 start_time）、
 * 进行中 → 已结束（超过 end_time），并同步将已结束券下未使用的用户券标记为过期。
 * 任务每 30 秒执行一次（上次执行结束后开始计时），异常仅记录告警日志，不影响后续调度。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillStatusScheduler {

    /** 秒杀券数据访问组件，用于批量更新券状态。 */
    private final SeckillCouponMapper seckillCouponMapper;
    /** 用户优惠券数据访问组件，用于批量更新过期状态。 */
    private final UserCouponMapper userCouponMapper;

    /**
     * 刷新秒杀券与用户券状态，每 30 秒执行一次。
     *
     * <p>分别将到点开始的券置为进行中、到点结束的券置为已结束，
     * 并将已结束券对应的未使用用户券置为过期；仅在有变更时输出 info 日志。</p>
     */
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
