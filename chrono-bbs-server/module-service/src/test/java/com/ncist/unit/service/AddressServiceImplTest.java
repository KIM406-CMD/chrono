package com.ncist.unit.service;

import com.ncist.common.enums.AddressErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.dto.AddressDTO;
import com.ncist.entity.Address;
import com.ncist.mapper.AddressMapper;
import com.ncist.service.impl.AddressServiceImpl;
import com.ncist.vo.AddressVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AddressServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("收货地址服务单元测试")
class AddressServiceImplTest {

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address address(long id, long userId, int isDefault) {
        return Address.builder()
                .id(id).userId(userId)
                .receiverName("张三").phone("13800138000")
                .province("湖北省").city("武汉市").district("洪山区")
                .detail("关山大道 100 号").isDefault(isDefault)
                .build();
    }

    private AddressDTO dto(Integer isDefault) {
        AddressDTO dto = new AddressDTO();
        dto.setReceiverName("李四");
        dto.setPhone("13900139000");
        dto.setProvince("湖南省");
        dto.setCity("长沙市");
        dto.setDistrict("岳麓区");
        dto.setDetail("麓山路 200 号");
        dto.setIsDefault(isDefault);
        return dto;
    }

    // ================ 查询 ================

    @Test
    @DisplayName("地址列表-默认地址在前")
    void listOrder() {
        when(addressMapper.selectByUserId(8L))
                .thenReturn(List.of(address(2L, 8L, 1), address(1L, 8L, 0)));

        List<AddressVO> list = addressService.list(8L);

        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getIsDefault());
        assertEquals("张三", list.get(0).getReceiverName());
        assertEquals(0, list.get(1).getIsDefault());
    }

    @Test
    @DisplayName("地址详情-不存在返回404")
    void detailNotFound() {
        when(addressMapper.selectById(1L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class, () -> addressService.detail(8L, 1L));
        assertEquals(AddressErrorCode.ADDRESS_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("地址详情-非属主返回403")
    void detailNotOwner() {
        when(addressMapper.selectById(1L)).thenReturn(address(1L, 9L, 0));

        BusinessException e = assertThrows(BusinessException.class, () -> addressService.detail(8L, 1L));
        assertEquals(AddressErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    // ================ 新增 ================

    @Test
    @DisplayName("新增地址-首个地址自动设为默认")
    void createFirstAutoDefault() {
        when(addressMapper.countByUserId(8L)).thenReturn(0);

        addressService.create(8L, dto(0));

        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getIsDefault());
        verify(addressMapper).clearDefault(8L);
    }

    @Test
    @DisplayName("新增地址-显式设默认时清除原默认")
    void createExplicitDefault() {
        when(addressMapper.countByUserId(8L)).thenReturn(3);

        addressService.create(8L, dto(1));

        verify(addressMapper).clearDefault(8L);
        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getIsDefault());
    }

    @Test
    @DisplayName("新增地址-普通新增不清除默认")
    void createNonDefault() {
        when(addressMapper.countByUserId(8L)).thenReturn(3);

        addressService.create(8L, dto(0));

        verify(addressMapper, never()).clearDefault(8L);
        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getIsDefault());
    }

    @Test
    @DisplayName("新增地址-超过20条返回400")
    void createLimitExceeded() {
        when(addressMapper.countByUserId(8L)).thenReturn(20);

        BusinessException e = assertThrows(BusinessException.class, () -> addressService.create(8L, dto(1)));
        assertEquals(AddressErrorCode.ADDRESS_LIMIT_EXCEEDED.getCode(), e.getCode());
        verify(addressMapper, never()).insert(any(Address.class));
    }

    // ================ 修改 ================

    @Test
    @DisplayName("修改地址-成功且设默认时清除原默认")
    void updateWithDefault() {
        when(addressMapper.selectById(1L)).thenReturn(address(1L, 8L, 0));

        addressService.update(8L, 1L, dto(1));

        verify(addressMapper).clearDefault(8L);
        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressMapper).update(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(1, captor.getValue().getIsDefault());
    }

    @Test
    @DisplayName("修改地址-非属主返回403")
    void updateNotOwner() {
        when(addressMapper.selectById(1L)).thenReturn(address(1L, 9L, 0));

        BusinessException e = assertThrows(BusinessException.class,
                () -> addressService.update(8L, 1L, dto(0)));
        assertEquals(AddressErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
        verify(addressMapper, never()).update(any(Address.class));
    }

    // ================ 默认地址/删除 ================

    @Test
    @DisplayName("设为默认地址-清除旧默认后设置新默认")
    void setDefault() {
        when(addressMapper.selectById(2L)).thenReturn(address(2L, 8L, 0));

        addressService.setDefault(8L, 2L);

        verify(addressMapper).clearDefault(8L);
        verify(addressMapper).setDefault(8L, 2L);
    }

    @Test
    @DisplayName("设为默认地址-非属主返回403")
    void setDefaultNotOwner() {
        when(addressMapper.selectById(2L)).thenReturn(address(2L, 9L, 0));

        BusinessException e = assertThrows(BusinessException.class, () -> addressService.setDefault(8L, 2L));
        assertEquals(AddressErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    @Test
    @DisplayName("删除地址-成功")
    void deleteSuccess() {
        when(addressMapper.selectById(1L)).thenReturn(address(1L, 8L, 0));

        addressService.delete(8L, 1L);

        verify(addressMapper).deleteById(8L, 1L);
    }

    @Test
    @DisplayName("删除地址-不存在返回404")
    void deleteNotFound() {
        when(addressMapper.selectById(1L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class, () -> addressService.delete(8L, 1L));
        assertEquals(AddressErrorCode.ADDRESS_NOT_FOUND.getCode(), e.getCode());
    }
}
