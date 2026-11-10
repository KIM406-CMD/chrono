package com.ncist.service;

import com.ncist.dto.AddressDTO;
import com.ncist.vo.AddressVO;

import java.util.List;

/**
 * 收货地址服务。
 *
 * <p>负责当前登录用户收货地址的查询、新增、修改、删除与默认地址管理，
 * 所有读写操作都会校验地址属主，防止越权访问或操作他人地址。</p>
 *
 * <p>新增与修改地址涉及“清除原默认地址 + 写入新地址”的多次写操作，
 * 由实现类通过事务保证一致性；单个用户最多保留 20 条地址，
 * 首个地址会自动设为默认地址。</p>
 */
public interface AddressService {

    /**
     * 查询当前用户的地址列表，默认地址排在首位。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @return 地址列表；无地址时返回空列表
     */
    List<AddressVO> list(Long userId);

    /**
     * 查询地址详情。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param id     地址 ID
     * @return 地址详情
     * @throws BusinessException 地址不存在或不属于当前用户时抛出
     */
    AddressVO detail(Long userId, Long id);

    /**
     * 新增收货地址。
     *
     * <p>用户地址数量达到 20 条时拒绝新增；新增的是用户首个地址时，
     * 无论请求是否标记默认，都会自动设为默认地址。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    地址信息，包含收货人、电话、省市区与详细地址等
     * @return 新增地址的 ID
     * @throws BusinessException 地址数量超过上限时抛出
     */
    Long create(Long userId, AddressDTO dto);

    /**
     * 修改收货地址。
     *
     * <p>请求标记为默认地址时，会先清除该用户原有的默认地址，保证默认地址唯一。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param id     待修改的地址 ID
     * @param dto    新的地址信息
     * @throws BusinessException 地址不存在或不属于当前用户时抛出
     */
    void update(Long userId, Long id, AddressDTO dto);

    /**
     * 将指定地址设为默认地址，并清除原默认地址。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param id     地址 ID
     * @throws BusinessException 地址不存在或不属于当前用户时抛出
     */
    void setDefault(Long userId, Long id);

    /**
     * 删除收货地址。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param id     地址 ID
     * @throws BusinessException 地址不存在或不属于当前用户时抛出
     */
    void delete(Long userId, Long id);
}
