package com.ncist.service;

import com.ncist.dto.CartAddDTO;
import com.ncist.dto.CartSelectDTO;
import com.ncist.dto.CartUpdateDTO;
import com.ncist.vo.CartItemVO;

import java.util.List;

/**
 * 购物车服务
 */
public interface CartService {

    /** 购物车列表（联查商品/SKU/商户） */
    List<CartItemVO> list(Long userId);

    /** 加入购物车（同 SKU 累加数量，校验上架状态与库存） */
    Long add(Long userId, CartAddDTO dto);

    /** 修改数量（校验库存） */
    void updateQuantity(Long userId, CartUpdateDTO dto);

    /** 选中/取消选中（支持单个/批量/全部） */
    void select(Long userId, CartSelectDTO dto);

    /** 全选/取消全选 */
    void selectAll(Long userId, Integer selected);

    /** 删除单个购物车项 */
    void remove(Long userId, Long id);

    /** 清空购物车 */
    void clear(Long userId);
}
