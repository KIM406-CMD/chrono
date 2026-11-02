package com.ncist.service.impl;

import com.ncist.common.enums.AddressErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.dto.AddressDTO;
import com.ncist.entity.Address;
import com.ncist.mapper.AddressMapper;
import com.ncist.service.AddressService;
import com.ncist.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收货地址服务实现
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private static final int MAX_ADDRESS_COUNT = 20;

    private final AddressMapper addressMapper;

    @Override
    public List<AddressVO> list(Long userId) {
        return addressMapper.selectByUserId(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressVO detail(Long userId, Long id) {
        return toVO(requireOwnedAddress(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, AddressDTO dto) {
        if (addressMapper.countByUserId(userId) >= MAX_ADDRESS_COUNT) {
            throw new BusinessException(AddressErrorCode.ADDRESS_LIMIT_EXCEEDED);
        }
        boolean makeDefault = Integer.valueOf(1).equals(dto.getIsDefault());
        if (!makeDefault && addressMapper.countByUserId(userId) == 0) {
            // 首个地址自动设为默认
            makeDefault = true;
        }
        if (makeDefault) {
            addressMapper.clearDefault(userId);
        }
        Address address = Address.builder()
                .userId(userId)
                .receiverName(dto.getReceiverName())
                .phone(dto.getPhone())
                .province(dto.getProvince())
                .city(dto.getCity())
                .district(dto.getDistrict())
                .detail(dto.getDetail())
                .isDefault(makeDefault ? 1 : 0)
                .build();
        addressMapper.insert(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long id, AddressDTO dto) {
        requireOwnedAddress(id, userId);
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            addressMapper.clearDefault(userId);
        }
        Address address = Address.builder()
                .id(id)
                .userId(userId)
                .receiverName(dto.getReceiverName())
                .phone(dto.getPhone())
                .province(dto.getProvince())
                .city(dto.getCity())
                .district(dto.getDistrict())
                .detail(dto.getDetail())
                .isDefault(dto.getIsDefault())
                .build();
        addressMapper.update(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        requireOwnedAddress(id, userId);
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(userId, id);
    }

    @Override
    public void delete(Long userId, Long id) {
        requireOwnedAddress(id, userId);
        addressMapper.deleteById(userId, id);
    }

    // 查询地址并校验属主
    private Address requireOwnedAddress(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null) {
            throw new BusinessException(AddressErrorCode.ADDRESS_NOT_FOUND);
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(AddressErrorCode.NO_PERMISSION_OPERATE);
        }
        return address;
    }

    private AddressVO toVO(Address address) {
        return AddressVO.builder()
                .id(address.getId())
                .userId(address.getUserId())
                .receiverName(address.getReceiverName())
                .phone(address.getPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detail(address.getDetail())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
