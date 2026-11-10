package com.ncist.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.context.UserContext;
import com.ncist.common.enums.AdminErrorCode;
import com.ncist.common.enums.ProductErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.entity.Product;
import com.ncist.mapper.CategoryMapper;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.mapper.ProductMapper;
import com.ncist.mapper.ProductSkuMapper;
import com.ncist.mapper.UserMapper;
import com.ncist.service.ProductSearchIndexService;
import com.ncist.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AdminServiceImpl 单元测试（管理员商品操作）。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("管理后台服务单元测试")
class AdminServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private MerchantMapper merchantMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductSkuMapper productSkuMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ProductSearchIndexService productSearchIndexService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        UserContext.set(new UserContext.UserInfo(1L, "admin", 3));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private Product product(long id) {
        return Product.builder().id(id).build();
    }

    @Test
    @DisplayName("管理员上下架-成功并同步搜索索引")
    void updateStatusSuccess() {
        when(productMapper.selectById(1L)).thenReturn(product(1L));

        adminService.updateProductStatus(1L, 0);

        verify(productMapper).updateStatus(1L, 0);
        verify(productSearchIndexService).syncProduct(1L);
    }

    @Test
    @DisplayName("管理员上下架-状态非法返回400")
    void updateStatusIllegal() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> adminService.updateProductStatus(1L, 2));

        assertEquals(ProductErrorCode.STATUS_ILLEGAL.getCode(), e.getCode());
        verify(productMapper, never()).updateStatus(1L, 2);
    }

    @Test
    @DisplayName("管理员上下架-商品不存在返回404")
    void updateStatusNotFound() {
        when(productMapper.selectById(1L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> adminService.updateProductStatus(1L, 1));

        assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("管理员删除商品-成功并移除搜索索引")
    void deleteSuccess() {
        when(productMapper.selectById(1L)).thenReturn(product(1L));

        adminService.deleteProduct(1L);

        verify(productMapper).deleteById(1L);
        verify(productSkuMapper).deleteByProductId(1L);
        verify(productSearchIndexService).deleteProduct(1L);
    }

    @Test
    @DisplayName("管理员删除商品-存在交易引用返回409")
    void deleteReferenced() {
        when(productMapper.selectById(1L)).thenReturn(product(1L));
        when(productMapper.countCartReferences(1L)).thenReturn(0L);
        when(productMapper.countOrderReferences(1L)).thenReturn(1L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> adminService.deleteProduct(1L));

        assertEquals(ProductErrorCode.PRODUCT_REFERENCED.getCode(), e.getCode());
        verify(productMapper, never()).deleteById(1L);
        verify(productSearchIndexService, never()).deleteProduct(1L);
    }

    @Test
    @DisplayName("管理员商品操作-非管理员返回403")
    void nonAdminForbidden() {
        UserContext.set(new UserContext.UserInfo(2L, "user", 1));

        BusinessException e = assertThrows(BusinessException.class,
                () -> adminService.updateProductStatus(1L, 1));

        assertEquals(AdminErrorCode.NOT_ADMIN.getCode(), e.getCode());
    }
}
