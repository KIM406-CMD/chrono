package com.ncist.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.enums.CartErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.dto.CartAddDTO;
import com.ncist.dto.CartSelectDTO;
import com.ncist.dto.CartUpdateDTO;
import com.ncist.entity.CartItem;
import com.ncist.entity.CartItemDetail;
import com.ncist.entity.Product;
import com.ncist.entity.ProductSku;
import com.ncist.mapper.CartItemMapper;
import com.ncist.mapper.ProductMapper;
import com.ncist.mapper.ProductSkuMapper;
import com.ncist.service.CartService;
import com.ncist.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现。
 *
 * <p>购物车数据持久化在 MySQL，查询列表时联查商品、SKU 与商户信息；
 * 所有更新与删除 SQL 都携带用户 ID 条件，从数据访问层防止越权。</p>
 *
 * <p>加入购物车时校验商品上架状态与库存，同一 SKU 累加数量；
 * 商品图片以 JSON 数组存储，展示时解析首张图片，解析失败仅返回空图片。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    /** 购物车项数据访问组件。 */
    private final CartItemMapper cartItemMapper;
    /** SKU 数据访问组件，用于校验库存。 */
    private final ProductSkuMapper productSkuMapper;
    /** 商品数据访问组件，用于校验上架状态。 */
    private final ProductMapper productMapper;
    /** JSON 序列化组件，用于解析商品图片数组。 */
    private final ObjectMapper objectMapper;

    /** {@inheritDoc} */
    @Override
    public List<CartItemVO> list(Long userId) {
        return cartItemMapper.selectDetailByUserId(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * <p>先校验 SKU 与商品状态，再按“已存在则累加、否则新增”处理；
     * 累加后的目标数量与库存比较，超出时拒绝。</p>
     */
    @Override
    public Long add(Long userId, CartAddDTO dto) {
        ProductSku sku = productSkuMapper.selectById(dto.getSkuId());
        if (sku == null) {
            throw new BusinessException(CartErrorCode.SKU_NOT_FOUND);
        }
        Product product = productMapper.selectById(sku.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException(CartErrorCode.PRODUCT_OFF_SHELF);
        }
        // 同 SKU 累加数量
        CartItem existing = cartItemMapper.selectByUserAndSku(userId, dto.getSkuId());
        int target = dto.getQuantity();
        if (existing != null) {
            target = existing.getQuantity() + dto.getQuantity();
        }
        if (target > sku.getStock()) {
            throw new BusinessException(CartErrorCode.STOCK_INSUFFICIENT);
        }
        if (existing != null) {
            cartItemMapper.updateQuantity(existing.getId(), target);
            return existing.getId();
        }
        CartItem cartItem = CartItem.builder()
                .userId(userId)
                .productId(sku.getProductId())
                .skuId(dto.getSkuId())
                .quantity(dto.getQuantity())
                .selected(1)
                .build();
        cartItemMapper.insert(cartItem);
        return cartItem.getId();
    }

    /** {@inheritDoc} */
    @Override
    public void updateQuantity(Long userId, CartUpdateDTO dto) {
        CartItemDetail item = requireOwnedItem(dto.getCartItemId(), userId);
        if (dto.getQuantity() > item.getStock()) {
            throw new BusinessException(CartErrorCode.STOCK_INSUFFICIENT);
        }
        cartItemMapper.updateQuantity(dto.getCartItemId(), dto.getQuantity());
    }

    /**
     * {@inheritDoc}
     *
     * <p>同时支持单个 ID、批量 ID 列表；均未提供时更新该用户全部购物车项。
     * 更新 SQL 限定 user_id，避免越权修改他人购物车。</p>
     */
    @Override
    public void select(Long userId, CartSelectDTO dto) {
        Integer flag = dto.selectedFlag();
        if (flag == null || (flag != 0 && flag != 1)) {
            throw new BusinessException(CartErrorCode.SELECTED_ILLEGAL);
        }
        List<Long> ids = new ArrayList<>();
        if (dto.getCartItemId() != null) {
            ids.add(dto.getCartItemId());
        }
        if (dto.getCartItemIds() != null) {
            ids.addAll(dto.getCartItemIds());
        }
        if (ids.isEmpty()) {
            // 未指定 ID 时作用于全部购物车项（对齐前端全选开关）
            cartItemMapper.updateSelectedByUser(userId, flag);
            return;
        }
        // SQL 限定 user_id，天然防越权
        cartItemMapper.updateSelectedByIds(userId, ids, flag);
    }

    /** {@inheritDoc} */
    @Override
    public void selectAll(Long userId, Integer selected) {
        if (selected == null || (selected != 0 && selected != 1)) {
            throw new BusinessException(CartErrorCode.SELECTED_ILLEGAL);
        }
        cartItemMapper.updateSelectedByUser(userId, selected);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(Long userId, Long id) {
        requireOwnedItem(id, userId);
        cartItemMapper.deleteById(userId, id);
    }

    /** {@inheritDoc} */
    @Override
    public void clear(Long userId) {
        cartItemMapper.deleteByUserId(userId);
    }

    /**
     * 查询购物车项并校验属主。
     *
     * @param id     购物车项 ID
     * @param userId 当前用户 ID
     * @return 购物车项联查明细
     * @throws BusinessException 购物车项不存在或不属于当前用户时抛出
     */
    private CartItemDetail requireOwnedItem(Long id, Long userId) {
        CartItemDetail item = cartItemMapper.selectDetailById(id);
        if (item == null) {
            throw new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(CartErrorCode.NO_PERMISSION_OPERATE);
        }
        return item;
    }

    /**
     * 将购物车联查明细转换为视图对象。
     *
     * <p>商品图片取 JSON 数组首张；同时填充文档命名与前端命名的两组 SKU 字段。</p>
     *
     * @param detail 购物车联查明细
     * @return 购物车项视图对象
     */
    private CartItemVO toVO(CartItemDetail detail) {
        String firstImage = firstImage(detail.getProductImages());
        return CartItemVO.builder()
                .id(detail.getId())
                .userId(detail.getUserId())
                .productId(detail.getProductId())
                .skuId(detail.getSkuId())
                .quantity(detail.getQuantity())
                .selected(detail.getSelected())
                .createdAt(detail.getCreatedAt())
                .productName(detail.getProductName())
                .productImage(firstImage)
                .specText(detail.getSpecText())
                .price(detail.getPrice())
                .stock(detail.getStock())
                .skuSpecText(detail.getSpecText())
                .skuPrice(detail.getPrice())
                .skuStock(detail.getStock())
                .merchantId(detail.getMerchantId())
                .merchantName(detail.getMerchantName())
                .build();
    }

    /**
     * 解析商品图片 JSON 数组并返回首张图片。
     *
     * @param images 商品图片 JSON 数组
     * @return 首张图片 URL；数据为空或解析失败时返回 null
     */
    private String firstImage(String images) {
        if (images == null || images.isBlank()) {
            return null;
        }
        try {
            List<String> list = objectMapper.readValue(images, new TypeReference<List<String>>() {
            });
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            log.warn("商品图片 JSON 解析失败: {}", images);
            return null;
        }
    }
}
