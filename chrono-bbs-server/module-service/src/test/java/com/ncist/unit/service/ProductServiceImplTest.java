package com.ncist.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.ProductCreateDTO;
import com.ncist.dto.ProductSkuDTO;
import com.ncist.entity.Category;
import com.ncist.entity.Merchant;
import com.ncist.entity.Product;
import com.ncist.entity.ProductMinPrice;
import com.ncist.entity.ProductSku;
import com.ncist.mapper.CategoryMapper;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.ProductMapper;
import com.ncist.mapper.ProductSkuMapper;
import com.ncist.service.impl.ProductServiceImpl;
import com.ncist.vo.ProductDetailVO;
import com.ncist.vo.ProductVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProductServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("商品服务单元测试")
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductSkuMapper productSkuMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private MerchantMapper merchantMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ProductServiceImpl productService;

    // 构造审核通过的商家
    private Merchant approvedMerchant() {
        return Merchant.builder().id(88L).userId(1L).shopName("测试小店").status(1).build();
    }

    // 构造归属商家的商品
    private Product ownProduct() {
        return Product.builder().id(66L).name("旧商品").merchantId(88L).categoryId(11L)
                .images("[\"a.jpg\"]").status(0).sales(10).build();
    }

    // 构造建品 DTO
    private ProductCreateDTO createDTO() {
        return ProductCreateDTO.builder()
                .name("苹果").description("新鲜").images(List.of("a.jpg", "b.jpg"))
                .brand("果园").categoryId(11L)
                .skuList(List.of(ProductSkuDTO.builder().specIds("[1,2]").specText("红富士/大").price(1500).stock(10).build()))
                .build();
    }

    @Test
    @DisplayName("商品列表-正常返回并组装最低价")
    void listProducts() {
        when(productMapper.selectListPage(null, 0, 10)).thenReturn(List.of(
                Product.builder().id(66L).name("苹果").brand("果园").categoryId(11L)
                        .images("[\"a.jpg\"]").sales(10).build()));
        when(productMapper.countByCategory(null)).thenReturn(1L);
        ProductMinPrice mp = new ProductMinPrice();
        mp.setProductId(66L);
        mp.setMinPrice(1500);
        when(productSkuMapper.selectMinPrices(List.of(66L))).thenReturn(List.of(mp));

        PageResult<ProductVO> page = productService.listProducts(null, 1, 10);

        assertEquals(1L, page.getTotal());
        assertEquals("a.jpg", page.getList().get(0).getImage());
        assertEquals(1500, page.getList().get(0).getMinPrice());
    }

    @Test
    @DisplayName("商品详情-下架商品对游客不可见")
    void detailNotOnShelf() {
        when(productMapper.selectById(66L)).thenReturn(Product.builder().id(66L).status(0).merchantId(88L).build());

        BusinessException ex = assertThrows(BusinessException.class, () -> productService.getProductDetail(66L));
        assertEquals("商品不存在", ex.getMessage());
    }

    @Test
    @DisplayName("商品详情-正常返回含 SKU 与分类名")
    void detailSuccess() {
        when(productMapper.selectById(66L)).thenReturn(
                Product.builder().id(66L).name("苹果").merchantId(88L).categoryId(11L)
                        .images("[\"a.jpg\",\"b.jpg\"]").status(1).sales(10).description("甜").build());
        when(productSkuMapper.selectByProductId(66L)).thenReturn(List.of(
                ProductSku.builder().id(1L).specText("红富士/大").price(1500).stock(10).status(1).build()));
        when(categoryMapper.selectById(11L)).thenReturn(Category.builder().id(11L).name("水果").build());
        when(merchantMapper.selectById(88L)).thenReturn(approvedMerchant());

        ProductDetailVO vo = productService.getProductDetail(66L);

        assertEquals("苹果", vo.getName());
        assertEquals(2, vo.getImages().size());
        assertEquals(1, vo.getSkuList().size());
        assertEquals("水果", vo.getCategoryName());
        assertEquals("测试小店", vo.getShopName());
    }

    @Test
    @DisplayName("创建商品-非商家被拒绝")
    void createNotMerchant() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.createProduct(createDTO(), 1L));
        assertEquals("仅商家可操作", ex.getMessage());
    }

    @Test
    @DisplayName("创建商品-商家审核未通过被拒绝")
    void createNotApproved() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(
                Merchant.builder().id(88L).status(0).build());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.createProduct(createDTO(), 1L));
        assertEquals("商家入驻审核未通过", ex.getMessage());
    }

    @Test
    @DisplayName("创建商品-分类不存在被拒绝")
    void createCategoryMissing() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(approvedMerchant());
        when(categoryMapper.selectById(11L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.createProduct(createDTO(), 1L));
        assertEquals("分类不存在", ex.getMessage());
    }

    @Test
    @DisplayName("创建商品-SKU 为空被拒绝")
    void createSkuEmpty() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(approvedMerchant());
        when(categoryMapper.selectById(11L)).thenReturn(Category.builder().id(11L).build());
        ProductCreateDTO dto = ProductCreateDTO.builder().name("苹果").categoryId(11L).skuList(List.of()).build();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.createProduct(dto, 1L));
        assertEquals("SKU列表不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("创建商品-成功且默认下架")
    void createSuccess() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(approvedMerchant());
        when(categoryMapper.selectById(11L)).thenReturn(Category.builder().id(11L).build());

        Long id = productService.createProduct(createDTO(), 1L);

        // 捕获插入的商品，验证归属与默认下架
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(captor.capture());
        assertEquals(88L, captor.getValue().getMerchantId());
        assertEquals(0, captor.getValue().getStatus());
        assertEquals("[\"a.jpg\",\"b.jpg\"]", captor.getValue().getImages());
        // SKU 批量插入
        verify(productSkuMapper).insertBatch(anyList());
        assertNull(id);
    }

    @Test
    @DisplayName("上下架-非法状态值被拒绝")
    void statusIllegal() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.updateStatus(66L, 5, 1L));
        assertEquals("商品状态值非法，仅允许0-下架或1-上架", ex.getMessage());
    }

    @Test
    @DisplayName("上下架-非本人商品被拒绝")
    void statusNotOwn() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(approvedMerchant());
        when(productMapper.selectById(66L)).thenReturn(
                Product.builder().id(66L).merchantId(999L).build());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.updateStatus(66L, 1, 1L));
        assertEquals("无权操作他人商品", ex.getMessage());
    }

    @Test
    @DisplayName("上下架-本人商品成功")
    void statusSuccess() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(approvedMerchant());
        when(productMapper.selectById(66L)).thenReturn(ownProduct());

        productService.updateStatus(66L, 1, 1L);

        verify(productMapper).updateStatus(66L, 1);
    }

    @Test
    @DisplayName("删除商品-被交易引用时返回业务冲突")
    void deleteReferenced() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(approvedMerchant());
        when(productMapper.selectById(66L)).thenReturn(ownProduct());
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("FK"))
                .when(productMapper).deleteById(66L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.deleteProduct(66L, 1L));
        assertEquals("商品已被交易引用，无法删除，请下架", ex.getMessage());
    }

    @Test
    @DisplayName("修改商品-SKU 全量替换")
    void updateReplaceSku() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(approvedMerchant());
        when(productMapper.selectById(66L)).thenReturn(ownProduct());
        when(categoryMapper.selectById(11L)).thenReturn(Category.builder().id(11L).build());

        productService.updateProduct(66L, createDTO(), 1L);

        verify(productSkuMapper).deleteByProductId(66L);
        verify(productSkuMapper).insertBatch(anyList());
    }

    @Test
    @DisplayName("修改商品-商品不存在被拒绝")
    void updateNotFound() {
        when(merchantMapper.selectByUserId(1L)).thenReturn(approvedMerchant());
        when(productMapper.selectById(66L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.updateProduct(66L, createDTO(), 1L));
        assertEquals("商品不存在", ex.getMessage());
    }
}
