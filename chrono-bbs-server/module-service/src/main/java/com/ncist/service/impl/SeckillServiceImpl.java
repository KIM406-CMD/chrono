package com.ncist.service.impl;

import com.ncist.common.enums.SeckillErrorCode;
import com.ncist.common.enums.UserRoleEnum;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.SeckillCalculateDTO;
import com.ncist.dto.SeckillCouponDTO;
import com.ncist.entity.SeckillCoupon;
import com.ncist.entity.UserCoupon;
import com.ncist.entity.UserCouponDetail;
import com.ncist.mapper.SeckillCouponMapper;
import com.ncist.mapper.UserCouponMapper;
import com.ncist.service.SeckillService;
import com.ncist.vo.MyCouponVO;
import com.ncist.vo.SeckillCalculateVO;
import com.ncist.vo.SeckillClaimVO;
import com.ncist.vo.SeckillCouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀券服务实现。
 *
 * <p>负责秒杀券与用户优惠券的查询、抢领、后台管理与下单优惠计算。
 * 抢领使用 Redis 原子操作做前置限流，Redis 不可用时降级数据库原子扣减；
 * 数据库写入在事务中完成，失败时对 Redis 做尽力而为的补偿。</p>
 *
 * <p>抢领流程（api-doc 15.3）：</p>
 * <ol>
 *   <li>校验活动进行中（status==1 且当前时间在 start_time~end_time 之间）。</li>
 *   <li>Redis DECR seckill:remain:{couponId}，结果 &lt; 0 则回补并返回“已抢光”。</li>
 *   <li>Redis INCR seckill:claimed:{couponId}:{userId}，超过 per_user_limit 则回补并返回“已领取”。</li>
 *   <li>MySQL 写入 user_coupon 记录并同步扣减 remain_count（Redis 故障时降级为数据库原子扣减）。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    /** 秒杀券数据访问组件。 */
    private final SeckillCouponMapper seckillCouponMapper;
    /** 用户优惠券数据访问组件。 */
    private final UserCouponMapper userCouponMapper;
    /** Redis 字符串操作模板，用于库存与领取计数的原子操作。 */
    private final StringRedisTemplate stringRedisTemplate;

    /** Redis 剩余库存 key 前缀，完整 key 为 {@code seckill:remain:{couponId}}。 */
    private static final String REMAIN_KEY_PREFIX = "seckill:remain:";
    /** Redis 用户领取计数 key 前缀，完整 key 为 {@code seckill:claimed:{couponId}:{userId}}。 */
    private static final String CLAIMED_KEY_PREFIX = "seckill:claimed:";
    /** 秒杀券时间参数格式：yyyy-MM-dd HH:mm:ss。 */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ================ 查询 ================

    /**
     * {@inheritDoc}
     *
     * <p>分页参数兜底：页码至少为 1，每页默认 10 条、上限 100 条。</p>
     */
    @Override
    public PageResult<SeckillCouponVO> listCoupons(Integer status, Integer page, Integer size) {
        // 状态过滤：不传查全部可抢券（未开始+进行中）
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException(SeckillErrorCode.STATUS_ILLEGAL);
        }
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : Math.min(size, 100);
        List<SeckillCoupon> coupons = seckillCouponMapper.selectListPage(status, (p - 1) * s, s);
        long total = seckillCouponMapper.countList(status);
        List<SeckillCouponVO> list = coupons.stream().map(this::toCouponVO).collect(Collectors.toList());
        return PageResult.of(total, p, s, list);
    }

    /** {@inheritDoc} */
    @Override
    public SeckillCouponVO getCouponDetail(Long id) {
        return toCouponVO(requireCoupon(id));
    }

    /** {@inheritDoc} */
    @Override
    public PageResult<MyCouponVO> listMyCoupons(Long userId, Integer status, Integer page, Integer size) {
        // 状态过滤：0-未使用，1-已使用，2-已过期
        if (status != null && (status < 0 || status > 2)) {
            throw new BusinessException(SeckillErrorCode.STATUS_ILLEGAL);
        }
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : Math.min(size, 100);
        List<UserCouponDetail> details = userCouponMapper.selectMyPage(userId, status, (p - 1) * s, s);
        long total = userCouponMapper.countMy(userId, status);
        List<MyCouponVO> list = details.stream().map(this::toMyCouponVO).collect(Collectors.toList());
        return PageResult.of(total, p, s, list);
    }

    // ================ 抢领 ================

    /**
     * {@inheritDoc}
     *
     * <p>Redis 正常时以 DECR/INCR 完成前置限流，数据库仅做最终扣减；
     * Redis 异常时降级为数据库原子扣减并校验限领数量。
     * MySQL 写入失败时事务回滚，同时尽力补偿 Redis 库存与领取计数。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillClaimVO claimCoupon(Long couponId, Long userId) {
        // 1. 券存在且活动进行中
        SeckillCoupon coupon = requireCoupon(couponId);
        requireActive(coupon);
        // 2~3. Redis 防超卖 + 防超领；Redis 故障返回 false 走数据库降级
        boolean redisGuarded = tryRedisClaim(coupon, userId);
        try {
            if (!redisGuarded) {
                // 降级路径：数据库原子扣减库存
                if (seckillCouponMapper.decrementRemain(couponId) == 0) {
                    throw new BusinessException(SeckillErrorCode.SECKILL_SOLD_OUT);
                }
                // 数据库校验每人限领数量
                if (userCouponMapper.countClaimed(userId, couponId) >= coupon.getPerUserLimit()) {
                    seckillCouponMapper.incrementRemain(couponId);
                    throw new BusinessException(SeckillErrorCode.SECKILL_ALREADY_CLAIMED);
                }
            }
            // 4. 写入领券记录（默认未使用）
            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .status(0)
                    .build();
            userCouponMapper.insert(userCoupon);
            // 5. 同步扣减 MySQL 剩余数量（Redis 已先行扣减，此处保证最终一致）
            if (redisGuarded) {
                seckillCouponMapper.decrementRemain(couponId);
            }
            return SeckillClaimVO.builder()
                    .userCouponId(userCoupon.getId())
                    .couponName(coupon.getName())
                    .discountType(coupon.getDiscountType())
                    .discountValue(coupon.getDiscountValue())
                    .minAmount(coupon.getMinAmount())
                    .endTime(coupon.getEndTime())
                    .build();
        } catch (Exception e) {
            // MySQL 写入失败：事务回滚后补偿 Redis（回补库存、回退领取计数）
            if (redisGuarded) {
                compensateRedis(couponId, userId);
            }
            throw e;
        }
    }

    /**
     * Redis 抢领前置防护：DECR 剩余库存 + INCR 领取计数。
     *
     * <p>库存键与计数键按活动结束时间加 24 小时懒加载并设置 TTL；
     * 库存或限领校验失败时回补 Redis 后抛出业务异常；
     * Redis 其他异常返回 false，由调用方降级为数据库原子扣减。</p>
     *
     * @param coupon 秒杀券
     * @param userId 用户 ID
     * @return true 表示 Redis 侧已完成前置扣减；false 表示需降级数据库
     * @throws BusinessException 库存不足或超过单人限领时抛出
     */
    private boolean tryRedisClaim(SeckillCoupon coupon, Long userId) {
        try {
            String remainKey = REMAIN_KEY_PREFIX + coupon.getId();
            String claimedKey = CLAIMED_KEY_PREFIX + coupon.getId() + ":" + userId;
            // 活动结束后 24h 过期，避免残留
            Duration ttl = activityTtl(coupon);
            // 库存懒加载到 Redis（首次访问时以数据库剩余量初始化）
            stringRedisTemplate.opsForValue().setIfAbsent(remainKey, String.valueOf(coupon.getRemainCount()), ttl);
            // 原子扣减，结果 < 0 说明已抢光，回补后拒绝
            Long remain = stringRedisTemplate.opsForValue().decrement(remainKey);
            if (remain == null || remain < 0) {
                stringRedisTemplate.opsForValue().increment(remainKey);
                throw new BusinessException(SeckillErrorCode.SECKILL_SOLD_OUT);
            }
            // 领取计数懒加载 + 原子自增，超过限领数则回补后拒绝
            stringRedisTemplate.opsForValue().setIfAbsent(claimedKey, "0", ttl);
            Long claimed = stringRedisTemplate.opsForValue().increment(claimedKey);
            if (claimed == null || claimed > coupon.getPerUserLimit()) {
                stringRedisTemplate.opsForValue().decrement(claimedKey);
                stringRedisTemplate.opsForValue().increment(remainKey);
                throw new BusinessException(SeckillErrorCode.SECKILL_ALREADY_CLAIMED);
            }
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis 不可用，领券降级数据库原子扣减: couponId={}, {}", coupon.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Redis 补偿：回补库存并回退领取计数。
     *
     * <p>尽力而为，失败仅记录告警日志，不覆盖原始业务异常。</p>
     *
     * @param couponId 秒杀券 ID
     * @param userId   用户 ID
     */
    private void compensateRedis(Long couponId, Long userId) {
        try {
            stringRedisTemplate.opsForValue().increment(REMAIN_KEY_PREFIX + couponId);
            stringRedisTemplate.opsForValue().decrement(CLAIMED_KEY_PREFIX + couponId + ":" + userId);
        } catch (Exception e) {
            log.warn("Redis 补偿失败: couponId={}, userId={}", couponId, userId, e);
        }
    }

    /**
     * 计算 Redis 库存键的过期时间：活动剩余时长加 24 小时缓冲。
     *
     * @param coupon 秒杀券
     * @return 过期时长；计算结果为负时兜底为 1 小时
     */
    private Duration activityTtl(SeckillCoupon coupon) {
        Duration ttl = Duration.between(LocalDateTime.now(), coupon.getEndTime()).plus(Duration.ofHours(24));
        return ttl.isNegative() ? Duration.ofHours(1) : ttl;
    }

    // ================ 后台管理 ================

    /** {@inheritDoc} */
    @Override
    public Long createCoupon(SeckillCouponDTO dto, Integer role) {
        requireManagerRole(role);
        LocalDateTime startTime = parseTime(dto.getStartTime());
        LocalDateTime endTime = parseTime(dto.getEndTime());
        validateCouponTimes(startTime, endTime);
        validateDiscount(dto.getDiscountType(), dto.getDiscountValue());
        SeckillCoupon coupon = SeckillCoupon.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minAmount(dto.getMinAmount())
                .totalCount(dto.getTotalCount())
                .remainCount(dto.getTotalCount())
                .startTime(startTime)
                .endTime(endTime)
                .perUserLimit(dto.getPerUserLimit() == null ? 1 : dto.getPerUserLimit())
                .status(computeStatus(startTime, endTime))
                .build();
        seckillCouponMapper.insert(coupon);
        return coupon.getId();
    }

    /**
     * {@inheritDoc}
     *
     * <p>总量变化时按差值同步 MySQL 剩余数量；若 Redis 库存键已存在，
     * 同样按差值同步，同步失败只记录日志。</p>
     */
    @Override
    public void updateCoupon(Long id, SeckillCouponDTO dto, Integer role) {
        requireManagerRole(role);
        SeckillCoupon old = requireCoupon(id);
        LocalDateTime startTime = parseTime(dto.getStartTime());
        LocalDateTime endTime = parseTime(dto.getEndTime());
        validateCouponTimes(startTime, endTime);
        validateDiscount(dto.getDiscountType(), dto.getDiscountValue());
        // 总量变化同步剩余库存：remain_count += (新总量 - 旧总量)，Redis 库存键若存在则同步增量
        int delta = dto.getTotalCount() - old.getTotalCount();
        if (delta != 0) {
            seckillCouponMapper.syncTotalCount(id, delta, dto.getTotalCount());
            syncRemainKey(id, delta);
        }
        // 时间变更后按时间窗重算状态
        seckillCouponMapper.updateInfo(SeckillCoupon.builder()
                .id(id)
                .name(dto.getName())
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minAmount(dto.getMinAmount())
                .startTime(startTime)
                .endTime(endTime)
                .perUserLimit(dto.getPerUserLimit() == null ? 1 : dto.getPerUserLimit())
                .status(computeStatus(startTime, endTime))
                .build());
    }

    /** {@inheritDoc} */
    @Override
    public void deleteCoupon(Long id, Integer role) {
        requireManagerRole(role);
        SeckillCoupon coupon = requireCoupon(id);
        // 仅未开始的券可删除（未开始意味着无人可领取）
        if (coupon.getStatus() == null || coupon.getStatus() != 0) {
            throw new BusinessException(SeckillErrorCode.SECKILL_DELETE_ONLY_NOT_STARTED);
        }
        seckillCouponMapper.deleteById(id);
    }

    // ================ 优惠计算 ================

    /**
     * {@inheritDoc}
     *
     * <p>满减券优惠额不超过订单原价；折扣券以 discountValue=85 表示 8.5 折。</p>
     */
    @Override
    public SeckillCalculateVO calculate(SeckillCalculateDTO dto, Long userId) {
        long totalAmount = dto.getTotalAmount();
        // 不使用券：原价即实付
        if (dto.getUserCouponId() == null) {
            return SeckillCalculateVO.builder()
                    .originalAmount((int) totalAmount)
                    .discountAmount(0)
                    .finalAmount((int) totalAmount)
                    .couponName(null)
                    .build();
        }
        // 校验用户券：存在、归属当前用户、未使用
        UserCoupon userCoupon = userCouponMapper.selectById(dto.getUserCouponId());
        if (userCoupon == null || !userId.equals(userCoupon.getUserId())) {
            throw new BusinessException(SeckillErrorCode.COUPON_NOT_FOUND);
        }
        if (userCoupon.getStatus() == null || userCoupon.getStatus() != 0) {
            throw new BusinessException(SeckillErrorCode.COUPON_USED_OR_EXPIRED);
        }
        // 校验券未过期
        SeckillCoupon coupon = requireCoupon(userCoupon.getCouponId());
        if (coupon.getEndTime() != null && LocalDateTime.now().isAfter(coupon.getEndTime())) {
            throw new BusinessException(SeckillErrorCode.COUPON_USED_OR_EXPIRED);
        }
        // 校验满足最低消费门槛
        if (totalAmount < coupon.getMinAmount()) {
            throw new BusinessException(SeckillErrorCode.BELOW_MIN_AMOUNT);
        }
        long discountAmount = computeDiscountAmount(coupon, totalAmount);
        return SeckillCalculateVO.builder()
                .originalAmount((int) totalAmount)
                .discountAmount((int) discountAmount)
                .finalAmount((int) (totalAmount - discountAmount))
                .couponName(coupon.getName())
                .build();
    }

    /**
     * 计算优惠金额。
     *
     * <p>满减券优惠为面额且不超过原价；折扣券 discountValue=85 表示 8.5 折，
     * 优惠 = 原价 - 原价 × 折扣 / 100。</p>
     *
     * @param coupon      秒杀券
     * @param totalAmount 订单原价
     * @return 优惠金额
     */
    private long computeDiscountAmount(SeckillCoupon coupon, long totalAmount) {
        if (coupon.getDiscountType() != null && coupon.getDiscountType() == 2) {
            long finalAmount = totalAmount * coupon.getDiscountValue() / 100;
            return totalAmount - finalAmount;
        }
        return Math.min(coupon.getDiscountValue(), totalAmount);
    }

    // ================ 私有校验与组装 ================

    /**
     * 查询秒杀券，不存在时抛出业务异常。
     *
     * @param id 秒杀券 ID
     * @return 秒杀券实体
     * @throws BusinessException 秒杀券不存在时抛出
     */
    private SeckillCoupon requireCoupon(Long id) {
        SeckillCoupon coupon = seckillCouponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException(SeckillErrorCode.SECKILL_NOT_FOUND);
        }
        return coupon;
    }

    /**
     * 校验活动进行中：状态为 1 且当前时间处于活动时间窗内。
     *
     * @param coupon 秒杀券
     * @throws BusinessException 活动未开始、已结束或状态非法时抛出
     */
    private void requireActive(SeckillCoupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStatus() == null || coupon.getStatus() != 1
                || now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new BusinessException(SeckillErrorCode.SECKILL_NOT_ACTIVE);
        }
    }

    /**
     * 校验管理权限：仅商家或管理员可管理秒杀券。
     *
     * @param role 当前用户角色
     * @throws BusinessException 角色为空或不是商家/管理员时抛出
     */
    private void requireManagerRole(Integer role) {
        if (role == null || (role != UserRoleEnum.MERCHANT.getCode() && role != UserRoleEnum.ADMIN.getCode())) {
            throw new BusinessException(SeckillErrorCode.NO_PERMISSION_MANAGE);
        }
    }

    /**
     * 解析时间字符串，格式为 yyyy-MM-dd HH:mm:ss。
     *
     * @param time 时间字符串
     * @return 解析后的时间
     * @throws BusinessException 格式不合法时抛出
     */
    private LocalDateTime parseTime(String time) {
        try {
            return LocalDateTime.parse(time, TIME_FORMATTER);
        } catch (Exception e) {
            throw new BusinessException(SeckillErrorCode.TIME_FORMAT_ILLEGAL);
        }
    }

    /**
     * 校验活动结束时间晚于开始时间。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @throws BusinessException 时间范围非法时抛出
     */
    private void validateCouponTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(SeckillErrorCode.TIME_RANGE_ILLEGAL);
        }
    }

    /**
     * 校验优惠类型与优惠值。
     *
     * <p>类型仅支持 1-满减、2-折扣；折扣券的折扣值必须在 1~99 之间。</p>
     *
     * @param discountType  优惠类型
     * @param discountValue 优惠值
     * @throws BusinessException 类型或折扣值非法时抛出
     */
    private void validateDiscount(Integer discountType, Integer discountValue) {
        if (discountType == null || (discountType != 1 && discountType != 2)) {
            throw new BusinessException(SeckillErrorCode.DISCOUNT_TYPE_ILLEGAL);
        }
        if (discountType == 2 && (discountValue == null || discountValue < 1 || discountValue > 99)) {
            throw new BusinessException(SeckillErrorCode.DISCOUNT_VALUE_ILLEGAL);
        }
    }

    /**
     * 按时间窗推导秒杀券状态。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 0-未开始，1-进行中，2-已结束
     */
    private int computeStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return 0;
        }
        if (now.isAfter(endTime)) {
            return 2;
        }
        return 1;
    }

    /**
     * 编辑秒杀券后按增量同步 Redis 库存键。
     *
     * <p>仅在库存键已存在时调整；失败只记录告警日志。</p>
     *
     * @param couponId 秒杀券 ID
     * @param delta    总量变化量
     */
    private void syncRemainKey(Long couponId, int delta) {
        try {
            String remainKey = REMAIN_KEY_PREFIX + couponId;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(remainKey))) {
                stringRedisTemplate.opsForValue().increment(remainKey, delta);
            }
        } catch (Exception e) {
            log.warn("同步 Redis 库存键失败，忽略: couponId={}", couponId);
        }
    }

    /**
     * 将秒杀券实体转换为视图对象。
     *
     * @param coupon 秒杀券实体
     * @return 秒杀券视图对象
     */
    private SeckillCouponVO toCouponVO(SeckillCoupon coupon) {
        return SeckillCouponVO.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minAmount(coupon.getMinAmount())
                .totalCount(coupon.getTotalCount())
                .remainCount(coupon.getRemainCount())
                .startTime(coupon.getStartTime())
                .endTime(coupon.getEndTime())
                .perUserLimit(coupon.getPerUserLimit())
                .status(coupon.getStatus())
                .build();
    }

    /**
     * 将用户优惠券联查明细转换为视图对象。
     *
     * @param detail 用户优惠券联查明细
     * @return 我的优惠券视图对象
     */
    private MyCouponVO toMyCouponVO(UserCouponDetail detail) {
        return MyCouponVO.builder()
                .id(detail.getId())
                .couponId(detail.getCouponId())
                .couponName(detail.getCouponName())
                .discountType(detail.getDiscountType())
                .discountValue(detail.getDiscountValue())
                .minAmount(detail.getMinAmount())
                .status(detail.getStatus())
                .endTime(detail.getEndTime())
                .claimTime(detail.getClaimTime())
                .build();
    }
}
