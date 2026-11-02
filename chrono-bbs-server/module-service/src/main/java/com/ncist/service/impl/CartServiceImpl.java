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
 * 购物车服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<CartItemVO> list(Long userId) {
        return cartItemMapper.selectDetailByUserId(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

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

    @Override
    public void updateQuantity(Long userId, CartUpdateDTO dto) {
        CartItemDetail item = requireOwnedItem(dto.getCartItemId(), userId);
        if (dto.getQuantity() > item.getStock()) {
            throw new BusinessException(CartErrorCode.STOCK_INSUFFICIENT);
        }
        cartItemMapper.updateQuantity(dto.getCartItemId(), dto.getQuantity());
    }

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

    @Override
    public void selectAll(Long userId, Integer selected) {
        if (selected == null || (selected != 0 && selected != 1)) {
            throw new BusinessException(CartErrorCode.SELECTED_ILLEGAL);
        }
        cartItemMapper.updateSelectedByUser(userId, selected);
    }

    @Override
    public void remove(Long userId, Long id) {
        requireOwnedItem(id, userId);
        cartItemMapper.deleteById(userId, id);
    }

    @Override
    public void clear(Long userId) {
        cartItemMapper.deleteByUserId(userId);
    }

    // 查询购物车项并校验属主
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

    // 联查结果转 VO（images JSON 取第一张；同时提供文档命名与前端命名两组 SKU 字段）
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

    // images JSON 解析取第一张，异常返回 null
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
