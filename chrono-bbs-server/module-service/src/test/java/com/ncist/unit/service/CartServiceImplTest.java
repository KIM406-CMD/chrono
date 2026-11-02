package com.ncist.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
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
import com.ncist.service.impl.CartServiceImpl;
import com.ncist.vo.CartItemVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CartServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("购物车服务单元测试")
class CartServiceImplTest {

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductSkuMapper productSkuMapper;

    @Mock
    private ProductMapper productMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CartServiceImpl cartService;

    private ProductSku sku(long id, long productId, int price, int stock, int status) {
        return ProductSku.builder()
                .id(id).productId(productId).specIds("1,3").specText("红色 / 64GB")
                .price(price).stock(stock).status(status)
                .build();
    }

    private Product product(long id, int status) {
        return Product.builder().id(id).status(status)
                .images("[\"https://img.example.com/p1.jpg\",\"https://img.example.com/p2.jpg\"]")
                .build();
    }

    private CartItemDetail detail(long id, long userId, int quantity, int stock) {
        CartItemDetail d = new CartItemDetail();
        d.setId(id);
        d.setUserId(userId);
        d.setProductId(100L);
        d.setSkuId(10L);
        d.setQuantity(quantity);
        d.setSelected(1);
        d.setProductName("iPhone 15");
        d.setProductImages("[\"https://img.example.com/p1.jpg\"]");
        d.setSpecText("红色 / 64GB");
        d.setPrice(599900);
        d.setStock(stock);
        d.setMerchantId(1L);
        d.setMerchantName("E2E小店");
        return d;
    }

    // ================ 加入购物车 ================

    @Test
    @DisplayName("加入购物车-SKU不存在返回404")
    void addSkuNotFound() {
        when(productSkuMapper.selectById(10L)).thenReturn(null);

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(10L);
        dto.setQuantity(1);

        BusinessException e = assertThrows(BusinessException.class, () -> cartService.add(8L, dto));
        assertEquals(CartErrorCode.SKU_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("加入购物车-商品已下架返回400")
    void addProductOffShelf() {
        when(productSkuMapper.selectById(10L)).thenReturn(sku(10L, 100L, 599900, 99, 1));
        when(productMapper.selectById(100L)).thenReturn(product(100L, 0));

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(10L);
        dto.setQuantity(1);

        BusinessException e = assertThrows(BusinessException.class, () -> cartService.add(8L, dto));
        assertEquals(CartErrorCode.PRODUCT_OFF_SHELF.getCode(), e.getCode());
    }

    @Test
    @DisplayName("加入购物车-新项默认选中并写入")
    void addNewItem() {
        when(productSkuMapper.selectById(10L)).thenReturn(sku(10L, 100L, 599900, 99, 1));
        when(productMapper.selectById(100L)).thenReturn(product(100L, 1));
        when(cartItemMapper.selectByUserAndSku(8L, 10L)).thenReturn(null);

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(10L);
        dto.setQuantity(2);

        cartService.add(8L, dto);

        verify(cartItemMapper).insert(any(CartItem.class));
        verify(cartItemMapper, never()).updateQuantity(any(), any());
    }

    @Test
    @DisplayName("加入购物车-同SKU累加数量")
    void addExistingAccumulate() {
        when(productSkuMapper.selectById(10L)).thenReturn(sku(10L, 100L, 599900, 99, 1));
        when(productMapper.selectById(100L)).thenReturn(product(100L, 1));
        when(cartItemMapper.selectByUserAndSku(8L, 10L))
                .thenReturn(CartItem.builder().id(1L).quantity(2).build());

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(10L);
        dto.setQuantity(3);

        cartService.add(8L, dto);

        verify(cartItemMapper).updateQuantity(1L, 5);
        verify(cartItemMapper, never()).insert(any(CartItem.class));
    }

    @Test
    @DisplayName("加入购物车-累加后超库存返回400")
    void addAccumulateStockInsufficient() {
        when(productSkuMapper.selectById(10L)).thenReturn(sku(10L, 100L, 599900, 4, 1));
        when(productMapper.selectById(100L)).thenReturn(product(100L, 1));
        when(cartItemMapper.selectByUserAndSku(8L, 10L))
                .thenReturn(CartItem.builder().id(1L).quantity(2).build());

        CartAddDTO dto = new CartAddDTO();
        dto.setSkuId(10L);
        dto.setQuantity(3);

        BusinessException e = assertThrows(BusinessException.class, () -> cartService.add(8L, dto));
        assertEquals(CartErrorCode.STOCK_INSUFFICIENT.getCode(), e.getCode());
    }

    // ================ 修改数量 ================

    @Test
    @DisplayName("修改数量-项不存在返回404")
    void updateItemNotFound() {
        when(cartItemMapper.selectDetailById(1L)).thenReturn(null);

        CartUpdateDTO dto = new CartUpdateDTO();
        dto.setCartItemId(1L);
        dto.setQuantity(2);

        BusinessException e = assertThrows(BusinessException.class, () -> cartService.updateQuantity(8L, dto));
        assertEquals(CartErrorCode.CART_ITEM_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("修改数量-非属主返回403")
    void updateNotOwner() {
        when(cartItemMapper.selectDetailById(1L)).thenReturn(detail(1L, 9L, 1, 99));

        CartUpdateDTO dto = new CartUpdateDTO();
        dto.setCartItemId(1L);
        dto.setQuantity(2);

        BusinessException e = assertThrows(BusinessException.class, () -> cartService.updateQuantity(8L, dto));
        assertEquals(CartErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    @Test
    @DisplayName("修改数量-超过库存返回400")
    void updateStockInsufficient() {
        when(cartItemMapper.selectDetailById(1L)).thenReturn(detail(1L, 8L, 1, 4));

        CartUpdateDTO dto = new CartUpdateDTO();
        dto.setCartItemId(1L);
        dto.setQuantity(5);

        BusinessException e = assertThrows(BusinessException.class, () -> cartService.updateQuantity(8L, dto));
        assertEquals(CartErrorCode.STOCK_INSUFFICIENT.getCode(), e.getCode());
    }

    @Test
    @DisplayName("修改数量-成功")
    void updateSuccess() {
        when(cartItemMapper.selectDetailById(1L)).thenReturn(detail(1L, 8L, 1, 99));

        CartUpdateDTO dto = new CartUpdateDTO();
        dto.setCartItemId(1L);
        dto.setQuantity(5);

        cartService.updateQuantity(8L, dto);

        verify(cartItemMapper).updateQuantity(1L, 5);
    }

    // ================ 选中 ================

    @Test
    @DisplayName("选中-布尔true归一化为1并批量更新")
    void selectBatchBoolean() {
        CartSelectDTO dto = new CartSelectDTO();
        dto.setCartItemIds(List.of(1L, 2L));
        dto.setSelected(BooleanNode.getTrue());

        cartService.select(8L, dto);

        verify(cartItemMapper).updateSelectedByIds(8L, List.of(1L, 2L), 1);
    }

    @Test
    @DisplayName("选中-单个ID（api-doc形式，整型0）")
    void selectSingleById() {
        CartSelectDTO dto = new CartSelectDTO();
        dto.setCartItemId(1L);
        dto.setSelected(BooleanNode.getFalse());

        cartService.select(8L, dto);

        verify(cartItemMapper).updateSelectedByIds(eq(8L), eq(List.of(1L)), eq(0));
    }

    @Test
    @DisplayName("选中-未指定ID时全选")
    void selectAllItemsWhenNoIds() {
        CartSelectDTO dto = new CartSelectDTO();
        dto.setSelected(BooleanNode.getTrue());

        cartService.select(8L, dto);

        verify(cartItemMapper).updateSelectedByUser(8L, 1);
        verify(cartItemMapper, never()).updateSelectedByIds(any(), any(), any());
    }

    @Test
    @DisplayName("选中-selected非法值返回400")
    void selectIllegalValue() {
        CartSelectDTO dto = new CartSelectDTO();
        dto.setCartItemId(1L);
        dto.setSelected(IntNode.valueOf(2));

        BusinessException e = assertThrows(BusinessException.class, () -> cartService.select(8L, dto));
        assertEquals(CartErrorCode.SELECTED_ILLEGAL.getCode(), e.getCode());
    }

    // ================ 删除/清空 ================

    @Test
    @DisplayName("删除购物车项-成功")
    void removeSuccess() {
        when(cartItemMapper.selectDetailById(1L)).thenReturn(detail(1L, 8L, 1, 99));

        cartService.remove(8L, 1L);

        verify(cartItemMapper).deleteById(8L, 1L);
    }

    @Test
    @DisplayName("清空购物车-成功")
    void clearSuccess() {
        cartService.clear(8L);

        verify(cartItemMapper).deleteByUserId(8L);
    }

    // ================ 列表 ================

    @Test
    @DisplayName("购物车列表-联查组装双命名SKU字段并取首图")
    void listAssemblesVO() {
        when(cartItemMapper.selectDetailByUserId(8L)).thenReturn(List.of(detail(1L, 8L, 2, 99)));

        List<CartItemVO> list = cartService.list(8L);

        assertEquals(1, list.size());
        CartItemVO vo = list.get(0);
        assertEquals("iPhone 15", vo.getProductName());
        assertEquals("https://img.example.com/p1.jpg", vo.getProductImage());
        assertEquals("红色 / 64GB", vo.getSpecText());
        assertEquals(vo.getSpecText(), vo.getSkuSpecText());
        assertEquals(599900, vo.getPrice());
        assertEquals(vo.getPrice(), vo.getSkuPrice());
        assertEquals(99, vo.getStock());
        assertEquals(vo.getStock(), vo.getSkuStock());
        assertEquals("E2E小店", vo.getMerchantName());
    }

    @Test
    @DisplayName("购物车列表-图片JSON异常时返回null不抛错")
    void listBadImageJson() {
        CartItemDetail d = detail(1L, 8L, 2, 99);
        d.setProductImages("not-json");
        when(cartItemMapper.selectDetailByUserId(8L)).thenReturn(List.of(d));

        List<CartItemVO> list = cartService.list(8L);

        assertNull(list.get(0).getProductImage());
    }
}
