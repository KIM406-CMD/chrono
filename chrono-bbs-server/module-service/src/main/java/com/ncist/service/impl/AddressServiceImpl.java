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
 * 收货地址服务实现。
 *
 * <p>基于 {@link AddressMapper} 完成地址增删改查，所有写操作都限定当前用户，
 * 并通过 {@link BusinessException} 返回“地址不存在”“无权限操作”等业务错误。</p>
 *
 * <p>新增、修改与设为默认涉及“清除原默认地址 + 写入/更新地址”的多次写操作，
 * 使用 {@code @Transactional} 保证原子性；删除与查询为单表单语句操作。</p>
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    /** 单个用户允许保存的收货地址数量上限。 */
    private static final int MAX_ADDRESS_COUNT = 20;

    /** 收货地址数据访问组件。 */
    private final AddressMapper addressMapper;

    /** {@inheritDoc} */
    @Override
    public List<AddressVO> list(Long userId) {
        return addressMapper.selectByUserId(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public AddressVO detail(Long userId, Long id) {
        return toVO(requireOwnedAddress(id, userId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>先统计地址数量做上限校验，再在需要时清除原默认地址并插入新地址；
     * 事务保证默认地址切换与新增操作的一致性。</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>标记为默认地址时先清除该用户原默认地址，再执行更新，整体置于事务中。</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>先清除原默认地址再设置新默认地址，事务保证同一用户默认地址唯一。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        requireOwnedAddress(id, userId);
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(userId, id);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(Long userId, Long id) {
        requireOwnedAddress(id, userId);
        addressMapper.deleteById(userId, id);
    }

    /**
     * 查询地址并校验属主。
     *
     * @param id     地址 ID
     * @param userId 当前用户 ID
     * @return 属于该用户的地址实体
     * @throws BusinessException 地址不存在或不属于该用户时抛出
     */
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

    /**
     * 将地址实体转换为视图对象。
     *
     * @param address 地址实体
     * @return 地址视图对象
     */
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
