package com.ncist.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.enums.MerchantErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.MerchantApplyDTO;
import com.ncist.entity.Merchant;
import com.ncist.entity.Order;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.service.impl.MerchantServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MerchantServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("商家服务单元测试")
class MerchantServiceImplTest {

    private static final Long USER_ID = 2L;
    private static final Long MERCHANT_ID = 1L;

    @Mock
    private MerchantMapper merchantMapper;
    @Mock
    private OrderMapper orderMapper;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MerchantServiceImpl merchantService;

    private Merchant merchant(int status) {
        return Merchant.builder().id(MERCHANT_ID).userId(USER_ID).shopName("测试店铺").status(status).build();
    }

    private MerchantApplyDTO applyDTO() {
        MerchantApplyDTO dto = new MerchantApplyDTO();
        dto.setShopName("测试店铺");
        dto.setContactPhone("13900000000");
        return dto;
    }

    @Test
    @DisplayName("发货-已支付且商品归属商家时成功")
    void shipSuccess() {
        when(merchantMapper.selectByUserId(USER_ID)).thenReturn(merchant(1));
        when(orderMapper.selectById(50001L)).thenReturn(Order.builder().id(50001L).status(2).build());
        when(merchantMapper.countOrderItemsNotBelongTo(50001L, MERCHANT_ID)).thenReturn(0);
        when(orderMapper.markShipped(50001L)).thenReturn(1);

        merchantService.ship(USER_ID, 50001L);

        verify(orderMapper).markShipped(50001L);
    }

    @Test
    @DisplayName("发货-订单包含其他商家商品时拒绝")
    void shipNotBelong() {
        when(merchantMapper.selectByUserId(USER_ID)).thenReturn(merchant(1));
        when(orderMapper.selectById(50001L)).thenReturn(Order.builder().id(50001L).status(2).build());
        when(merchantMapper.countOrderItemsNotBelongTo(50001L, MERCHANT_ID)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> merchantService.ship(USER_ID, 50001L));

        assertEquals(MerchantErrorCode.ORDER_NOT_BELONG_TO_YOU.getCode(), e.getCode());
        verify(orderMapper, never()).markShipped(50001L);
    }

    @Test
    @DisplayName("发货-订单状态不允许时返回业务错误")
    void shipIllegalStatus() {
        when(merchantMapper.selectByUserId(USER_ID)).thenReturn(merchant(1));
        when(orderMapper.selectById(50001L)).thenReturn(Order.builder().id(50001L).status(1).build());
        when(merchantMapper.countOrderItemsNotBelongTo(50001L, MERCHANT_ID)).thenReturn(0);
        when(orderMapper.markShipped(50001L)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> merchantService.ship(USER_ID, 50001L));

        assertEquals(MerchantErrorCode.SHIPPED_STATUS_ILLEGAL.getCode(), e.getCode());
    }

    @Test
    @DisplayName("入驻申请-重复申请返回业务错误")
    void applyDuplicate() {
        when(merchantMapper.selectByUserId(USER_ID)).thenReturn(merchant(1));

        BusinessException e = assertThrows(BusinessException.class,
                () -> merchantService.apply(USER_ID, applyDTO()));

        assertEquals(MerchantErrorCode.ALREADY_MERCHANT.getCode(), e.getCode());
        verify(merchantMapper, never()).insertApply(org.mockito.ArgumentMatchers.any(Merchant.class));
    }

    @Test
    @DisplayName("商家信息-非商家返回业务错误")
    void getInfoNotMerchant() {
        when(merchantMapper.selectByUserId(USER_ID)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> merchantService.getInfo(USER_ID));

        assertEquals(MerchantErrorCode.NOT_MERCHANT.getCode(), e.getCode());
    }

    @Test
    @DisplayName("商家商品列表-无商品返回空分页")
    void listProductsEmpty() {
        when(merchantMapper.selectByUserId(USER_ID)).thenReturn(merchant(1));
        when(merchantMapper.countMerchantProducts(MERCHANT_ID, null)).thenReturn(0L);

        PageResult<Map<String, Object>> result = merchantService.listProducts(USER_ID, null, 1, 10);

        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getList().size());
    }
}
