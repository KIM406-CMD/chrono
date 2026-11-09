package com.ncist.service;

import com.ncist.dto.CartAddDTO;
import com.ncist.dto.CartSelectDTO;
import com.ncist.dto.CartUpdateDTO;
import com.ncist.vo.CartItemVO;

import java.util.List;

/**
 * 购物车服务。
 *
 * <p>负责当前用户购物车项的查询、加入、数量调整、选中状态切换与删除。
 * 所有写操作均以用户 ID 作为数据边界，防止越权操作他人购物车。</p>
 *
 * <p>加入购物车时会校验商品上架状态与 SKU 库存；同一 SKU 重复加入时累加数量。
 * 购物车数据直接持久化到数据库，不使用缓存。</p>
 */
public interface CartService {

    /**
     * 查询当前用户购物车列表，联查商品、SKU 与商户信息。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @return 购物车项列表；购物车为空时返回空列表
     */
    List<CartItemVO> list(Long userId);

    /**
     * 将 SKU 加入购物车。
     *
     * <p>校验 SKU 存在、商品已上架且累计数量不超过库存；
     * 同一 SKU 已存在时在原有数量上累加，否则新增购物车项并默认选中。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    加入参数，包含 SKU ID 与购买数量
     * @return 购物车项 ID（新增或已存在项的 ID）
     * @throws BusinessException SKU 不存在、商品已下架或库存不足时抛出
     */
    Long add(Long userId, CartAddDTO dto);

    /**
     * 修改购物车项数量。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    修改参数，包含购物车项 ID 与新数量
     * @throws BusinessException 购物车项不存在、不属于当前用户或库存不足时抛出
     */
    void updateQuantity(Long userId, CartUpdateDTO dto);

    /**
     * 选中或取消选中购物车项。
     *
     * <p>支持单个 ID、批量 ID 列表；两者均未提供时作用于该用户的全部购物车项。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    选中参数，包含购物车项 ID/ID 列表与选中标记（0-取消，1-选中）
     * @throws BusinessException 选中标记非法时抛出
     */
    void select(Long userId, CartSelectDTO dto);

    /**
     * 全选或取消全选当前用户的购物车。
     *
     * @param userId   用户 ID，必须为当前登录用户
     * @param selected 选中标记：0-取消全选，1-全选
     * @throws BusinessException 选中标记非法时抛出
     */
    void selectAll(Long userId, Integer selected);

    /**
     * 删除单个购物车项。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param id     购物车项 ID
     * @throws BusinessException 购物车项不存在或不属于当前用户时抛出
     */
    void remove(Long userId, Long id);

    /**
     * 清空当前用户的购物车。
     *
     * @param userId 用户 ID，必须为当前登录用户
     */
    void clear(Long userId);
}
