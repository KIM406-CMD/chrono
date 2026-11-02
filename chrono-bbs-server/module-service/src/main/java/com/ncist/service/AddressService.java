package com.ncist.service;

import com.ncist.dto.AddressDTO;
import com.ncist.vo.AddressVO;

import java.util.List;

/**
 * 收货地址服务
 */
public interface AddressService {

    /** 地址列表（默认地址在前） */
    List<AddressVO> list(Long userId);

    /** 地址详情（属主校验） */
    AddressVO detail(Long userId, Long id);

    /** 新增地址（最多 20 条；首个地址自动设为默认） */
    Long create(Long userId, AddressDTO dto);

    /** 修改地址（属主校验；设为默认时清除原默认） */
    void update(Long userId, Long id, AddressDTO dto);

    /** 设为默认地址 */
    void setDefault(Long userId, Long id);

    /** 删除地址 */
    void delete(Long userId, Long id);
}
