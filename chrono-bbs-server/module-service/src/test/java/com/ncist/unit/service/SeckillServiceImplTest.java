package com.ncist.unit.service;

import com.ncist.common.enums.SeckillErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.SeckillCalculateDTO;
import com.ncist.dto.SeckillCouponDTO;
import com.ncist.entity.SeckillCoupon;
import com.ncist.entity.UserCoupon;
import com.ncist.entity.UserCouponDetail;
import com.ncist.mapper.SeckillCouponMapper;
import com.ncist.mapper.UserCouponMapper;
import com.ncist.service.impl.SeckillServiceImpl;
import com.ncist.vo.MyCouponVO;
import com.ncist.vo.SeckillCalculateVO;
import com.ncist.vo.SeckillClaimVO;
import com.ncist.vo.SeckillCouponVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SeckillServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("秒杀券服务单元测试")
class SeckillServiceImplTest {

    @Mock
    private SeckillCouponMapper seckillCouponMapper;

    @Mock
    private UserCouponMapper userCouponMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SeckillServiceImpl seckillService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 进行中的满减券：满 50 减 10，总量 100 剩 99，每人限领 1
    private SeckillCoupon activeCoupon(long id, int perUserLimit) {
        return SeckillCoupon.builder()
                .id(id).name("新人满减券").description("新用户专享")
                .discountType(1).discountValue(1000).minAmount(5000)
                .totalCount(100).remainCount(99)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(30))
                .perUserLimit(perUserLimit).status(1)
                .build();
    }

    private SeckillCouponDTO couponDTO(String start, String end, int totalCount) {
        return SeckillCouponDTO.builder()
                .name("新人满减券").description("新用户专享")
                .discountType(1).discountValue(1000).minAmount(5000)
                .totalCount(totalCount).startTime(start).endTime(end)
                .build();
    }

    private String format(LocalDateTime time) {
        return time.format(TIME_FORMATTER);
    }

    // ================ 列表/详情 ================

    @Test
    @DisplayName("秒杀券列表-默认查询全部可抢券并分页")
    void listCouponsDefault() {
        when(seckillCouponMapper.selectListPage(null, 0, 10)).thenReturn(List.of(activeCoupon(1L, 1)));
        when(seckillCouponMapper.countList(null)).thenReturn(1L);

        PageResult<SeckillCouponVO> result = seckillService.listCoupons(null, null, null);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("新人满减券", result.getList().get(0).getName());
        assertEquals(99, result.getList().get(0).getRemainCount());
        verify(seckillCouponMapper).selectListPage(null, 0, 10);
    }

    @Test
    @DisplayName("秒杀券列表-非法状态参数被拒绝")
    void listCouponsIllegalStatus() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.listCoupons(5, 1, 10));
        assertEquals(SeckillErrorCode.STATUS_ILLEGAL.getMessage(), e.getMessage());
    }

    @Test
    @DisplayName("秒杀券详情-不存在时返回404")
    void getCouponDetailNotFound() {
        when(seckillCouponMapper.selectById(99L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.getCouponDetail(99L));
        assertEquals(SeckillErrorCode.SECKILL_NOT_FOUND.getMessage(), e.getMessage());
    }

    // ================ 抢领 ================

    @Test
    @DisplayName("抢领-券不存在返回404")
    void claimCouponNotFound() {
        when(seckillCouponMapper.selectById(99L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.claimCoupon(99L, 1L));
        assertEquals(SeckillErrorCode.SECKILL_NOT_FOUND.getMessage(), e.getMessage());
    }

    @Test
    @DisplayName("抢领-活动未开始返回400")
    void claimCouponNotActive() {
        SeckillCoupon coupon = activeCoupon(1L, 1);
        coupon.setStatus(0);
        when(seckillCouponMapper.selectById(1L)).thenReturn(coupon);

        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.claimCoupon(1L, 1L));
        assertEquals(SeckillErrorCode.SECKILL_NOT_ACTIVE.getMessage(), e.getMessage());
    }

    @Test
    @DisplayName("抢领-Redis售罄返回409并回补库存")
    void claimCouponSoldOut() {
        SeckillCoupon coupon = activeCoupon(1L, 1);
        when(seckillCouponMapper.selectById(1L)).thenReturn(coupon);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement("seckill:remain:1")).thenReturn(-1L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.claimCoupon(1L, 1L));

        assertEquals(SeckillErrorCode.SECKILL_SOLD_OUT.getMessage(), e.getMessage());
        // 售罄后必须回补库存键
        verify(valueOperations).increment("seckill:remain:1");
        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
    }

    @Test
    @DisplayName("抢领-超过限领数返回409并回补库存与计数")
    void claimCouponOverLimit() {
        SeckillCoupon coupon = activeCoupon(1L, 1);
        when(seckillCouponMapper.selectById(1L)).thenReturn(coupon);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement("seckill:remain:1")).thenReturn(98L);
        // 已领 1 次，再次领取后计数为 2，超过限领 1
        when(valueOperations.increment("seckill:claimed:1:1")).thenReturn(2L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.claimCoupon(1L, 1L));

        assertEquals(SeckillErrorCode.SECKILL_ALREADY_CLAIMED.getMessage(), e.getMessage());
        verify(valueOperations).decrement("seckill:claimed:1:1");
        verify(valueOperations).increment("seckill:remain:1");
        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
    }

    @Test
    @DisplayName("抢领-成功路径：Redis防护+MySQL写记录并同步扣减")
    void claimCouponSuccess() {
        SeckillCoupon coupon = activeCoupon(1L, 1);
        when(seckillCouponMapper.selectById(1L)).thenReturn(coupon);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement("seckill:remain:1")).thenReturn(98L);
        when(valueOperations.increment("seckill:claimed:1:1")).thenReturn(1L);
        doAnswer(invocation -> {
            UserCoupon uc = invocation.getArgument(0);
            uc.setId(88L);
            return 1;
        }).when(userCouponMapper).insert(any(UserCoupon.class));
        when(seckillCouponMapper.decrementRemain(1L)).thenReturn(1);

        SeckillClaimVO vo = seckillService.claimCoupon(1L, 1L);

        assertEquals(88L, vo.getUserCouponId());
        assertEquals("新人满减券", vo.getCouponName());
        assertEquals(1000, vo.getDiscountValue());
        verify(userCouponMapper).insert(any(UserCoupon.class));
        // Redis 路径需同步扣减 MySQL 库存
        verify(seckillCouponMapper).decrementRemain(1L);
    }

    @Test
    @DisplayName("抢领-Redis故障时降级数据库原子扣减")
    void claimCouponRedisFallback() {
        SeckillCoupon coupon = activeCoupon(1L, 1);
        when(seckillCouponMapper.selectById(1L)).thenReturn(coupon);
        when(stringRedisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));
        when(seckillCouponMapper.decrementRemain(1L)).thenReturn(1);
        when(userCouponMapper.countClaimed(1L, 1L)).thenReturn(0);
        doAnswer(invocation -> {
            UserCoupon uc = invocation.getArgument(0);
            uc.setId(89L);
            return 1;
        }).when(userCouponMapper).insert(any(UserCoupon.class));

        SeckillClaimVO vo = seckillService.claimCoupon(1L, 1L);

        assertNotNull(vo.getUserCouponId());
        // 降级路径只扣一次数据库库存（Redis 不可用不再二次扣减）
        verify(seckillCouponMapper).decrementRemain(1L);
    }

    @Test
    @DisplayName("抢领-降级路径库存不足返回409")
    void claimCouponFallbackSoldOut() {
        SeckillCoupon coupon = activeCoupon(1L, 1);
        when(seckillCouponMapper.selectById(1L)).thenReturn(coupon);
        when(stringRedisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));
        when(seckillCouponMapper.decrementRemain(1L)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.claimCoupon(1L, 1L));

        assertEquals(SeckillErrorCode.SECKILL_SOLD_OUT.getMessage(), e.getMessage());
        verify(userCouponMapper, never()).insert(any(UserCoupon.class));
    }

    // ================ 后台管理 ================

    @Test
    @DisplayName("创建秒杀券-普通用户无权限返回403")
    void createCouponNoPermission() {
        assertThrows(BusinessException.class,
                () -> seckillService.createCoupon(couponDTO(format(LocalDateTime.now().plusDays(1)),
                        format(LocalDateTime.now().plusDays(30)), 100), 1));
    }

    @Test
    @DisplayName("创建秒杀券-管理员成功且剩余等于总量、状态按时间推导")
    void createCouponSuccess() {
        doAnswer(invocation -> {
            SeckillCoupon coupon = invocation.getArgument(0);
            coupon.setId(66L);
            return 1;
        }).when(seckillCouponMapper).insert(any(SeckillCoupon.class));

        Long id = seckillService.createCoupon(couponDTO(format(LocalDateTime.now().plusDays(1)),
                format(LocalDateTime.now().plusDays(30)), 100), 3);

        assertEquals(66L, id);
        // 通过 ArgumentCaptor 断言插入实体：remain=total、状态按时间推导、限领默认 1
        org.mockito.ArgumentCaptor<SeckillCoupon> captor = org.mockito.ArgumentCaptor.forClass(SeckillCoupon.class);
        verify(seckillCouponMapper).insert(captor.capture());
        SeckillCoupon saved = captor.getValue();
        assertEquals(100, saved.getTotalCount());
        assertEquals(100, saved.getRemainCount());
        assertEquals(0, saved.getStatus());
        assertEquals(1, saved.getPerUserLimit());
    }

    @Test
    @DisplayName("创建秒杀券-时间格式错误返回400")
    void createCouponBadTimeFormat() {
        assertThrows(BusinessException.class, () ->
                seckillService.createCoupon(SeckillCouponDTO.builder()
                        .name("券").discountType(1).discountValue(1000).minAmount(0)
                        .totalCount(10).startTime("2026/01/01 00:00:00").endTime("2026-12-31 23:59:59")
                        .build(), 3));
    }

    @Test
    @DisplayName("创建秒杀券-结束时间不晚于开始时间返回400")
    void createCouponBadTimeRange() {
        String time = format(LocalDateTime.now().plusDays(10));
        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.createCoupon(couponDTO(time, time, 100), 3));
        assertEquals(SeckillErrorCode.TIME_RANGE_ILLEGAL.getMessage(), e.getMessage());
    }

    @Test
    @DisplayName("创建秒杀券-折扣券优惠值越界返回400")
    void createCouponDiscountValueIllegal() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.createCoupon(SeckillCouponDTO.builder()
                        .name("折扣券").discountType(2).discountValue(120).minAmount(0)
                        .totalCount(10).startTime(format(LocalDateTime.now().plusDays(1)))
                        .endTime(format(LocalDateTime.now().plusDays(30)))
                        .build(), 3));
        assertEquals(SeckillErrorCode.DISCOUNT_VALUE_ILLEGAL.getMessage(), e.getMessage());
    }

    @Test
    @DisplayName("编辑秒杀券-总量增量同步剩余库存")
    void updateCouponSyncTotal() {
        SeckillCoupon old = activeCoupon(1L, 1);
        old.setTotalCount(100);
        when(seckillCouponMapper.selectById(1L)).thenReturn(old);
        when(stringRedisTemplate.hasKey("seckill:remain:1")).thenReturn(false);

        seckillService.updateCoupon(1L, couponDTO(format(LocalDateTime.now().plusDays(1)),
                format(LocalDateTime.now().plusDays(30)), 200), 2);

        // 新总量 200 - 旧总量 100 = 增量 100 同步到剩余库存
        verify(seckillCouponMapper).syncTotalCount(1L, 100, 200);
        verify(seckillCouponMapper).updateInfo(any(SeckillCoupon.class));
    }

    @Test
    @DisplayName("删除秒杀券-进行中的券拒绝删除")
    void deleteCouponStarted() {
        when(seckillCouponMapper.selectById(1L)).thenReturn(activeCoupon(1L, 1));

        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.deleteCoupon(1L, 3));

        assertEquals(SeckillErrorCode.SECKILL_DELETE_ONLY_NOT_STARTED.getMessage(), e.getMessage());
        verify(seckillCouponMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("删除秒杀券-未开始的券可删除")
    void deleteCouponNotStarted() {
        SeckillCoupon coupon = activeCoupon(1L, 1);
        coupon.setStatus(0);
        when(seckillCouponMapper.selectById(1L)).thenReturn(coupon);

        seckillService.deleteCoupon(1L, 3);

        verify(seckillCouponMapper).deleteById(1L);
    }

    // ================ 我的优惠券 ================

    @Test
    @DisplayName("我的优惠券-联查分页并组装VO")
    void listMyCoupons() {
        UserCouponDetail detail = UserCouponDetail.builder()
                .id(1L).couponId(1L).couponName("新人满减券")
                .discountType(1).discountValue(1000).minAmount(5000)
                .status(0).endTime(LocalDateTime.now().plusDays(30))
                .claimTime(LocalDateTime.now())
                .build();
        when(userCouponMapper.selectMyPage(1L, 0, 0, 10)).thenReturn(List.of(detail));
        when(userCouponMapper.countMy(1L, 0)).thenReturn(1L);

        PageResult<MyCouponVO> result = seckillService.listMyCoupons(1L, 0, 1, 10);

        assertEquals(1L, result.getTotal());
        assertEquals("新人满减券", result.getList().get(0).getCouponName());
        assertEquals(0, result.getList().get(0).getStatus());
    }

    // ================ 优惠计算 ================

    @Test
    @DisplayName("计算优惠-不使用券时优惠为0")
    void calculateWithoutCoupon() {
        SeckillCalculateVO vo = seckillService.calculate(
                SeckillCalculateDTO.builder().userCouponId(null).totalAmount(15000).build(), 1L);

        assertEquals(15000, vo.getOriginalAmount());
        assertEquals(0, vo.getDiscountAmount());
        assertEquals(15000, vo.getFinalAmount());
        assertNull(vo.getCouponName());
    }

    @Test
    @DisplayName("计算优惠-满减券不满足最低消费返回400")
    void calculateBelowMinAmount() {
        UserCoupon userCoupon = UserCoupon.builder().id(1L).userId(1L).couponId(1L).status(0).build();
        when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);
        when(seckillCouponMapper.selectById(1L)).thenReturn(activeCoupon(1L, 1));

        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.calculate(SeckillCalculateDTO.builder()
                        .userCouponId(1L).totalAmount(4000).build(), 1L));

        assertEquals(SeckillErrorCode.BELOW_MIN_AMOUNT.getMessage(), e.getMessage());
    }

    @Test
    @DisplayName("计算优惠-已使用的券返回409")
    void calculateUsedCoupon() {
        UserCoupon userCoupon = UserCoupon.builder().id(1L).userId(1L).couponId(1L).status(1).build();
        when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);

        BusinessException e = assertThrows(BusinessException.class,
                () -> seckillService.calculate(SeckillCalculateDTO.builder()
                        .userCouponId(1L).totalAmount(15000).build(), 1L));

        assertEquals(SeckillErrorCode.COUPON_USED_OR_EXPIRED.getMessage(), e.getMessage());
    }

    @Test
    @DisplayName("计算优惠-他人券返回404")
    void calculateOthersCoupon() {
        UserCoupon userCoupon = UserCoupon.builder().id(1L).userId(2L).couponId(1L).status(0).build();
        when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);

        assertThrows(BusinessException.class,
                () -> seckillService.calculate(SeckillCalculateDTO.builder()
                        .userCouponId(1L).totalAmount(15000).build(), 1L));
    }

    @Test
    @DisplayName("计算优惠-满减券金额正确")
    void calculateFullReduction() {
        UserCoupon userCoupon = UserCoupon.builder().id(1L).userId(1L).couponId(1L).status(0).build();
        when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);
        when(seckillCouponMapper.selectById(1L)).thenReturn(activeCoupon(1L, 1));

        SeckillCalculateVO vo = seckillService.calculate(SeckillCalculateDTO.builder()
                .userCouponId(1L).totalAmount(15000).build(), 1L);

        assertEquals(15000, vo.getOriginalAmount());
        assertEquals(1000, vo.getDiscountAmount());
        assertEquals(14000, vo.getFinalAmount());
        assertEquals("新人满减券", vo.getCouponName());
    }

    @Test
    @DisplayName("计算优惠-折扣券（8.5折）金额正确")
    void calculateDiscountCoupon() {
        SeckillCoupon coupon = activeCoupon(1L, 1);
        coupon.setDiscountType(2);
        coupon.setDiscountValue(85);
        coupon.setMinAmount(0);
        UserCoupon userCoupon = UserCoupon.builder().id(1L).userId(1L).couponId(1L).status(0).build();
        when(userCouponMapper.selectById(1L)).thenReturn(userCoupon);
        when(seckillCouponMapper.selectById(1L)).thenReturn(coupon);

        SeckillCalculateVO vo = seckillService.calculate(SeckillCalculateDTO.builder()
                .userCouponId(1L).totalAmount(15000).build(), 1L);

        assertEquals(15000, vo.getOriginalAmount());
        // 15000 * 85 / 100 = 12750，优惠 2250
        assertEquals(2250, vo.getDiscountAmount());
        assertEquals(12750, vo.getFinalAmount());
        assertTrue(vo.getFinalAmount() > 0);
    }
}
