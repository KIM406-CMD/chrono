package com.ncist.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.enums.CategoryErrorCode;
import com.ncist.common.enums.ProductErrorCode;
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
import com.ncist.service.ProductService;
import com.ncist.vo.ProductDetailVO;
import com.ncist.vo.ProductSkuVO;
import com.ncist.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final CategoryMapper categoryMapper;
    private final MerchantMapper merchantMapper;
    private final ObjectMapper objectMapper;

    // 分页查询上架商品
    @Override
    public PageResult<ProductVO> listProducts(Long categoryId, Integer page, Integer size) {
        // 分页参数兜底：page 至少 1，size 默认 10、上限 100
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : Math.min(size, 100);
        // 查询商品分页
        List<Product> products = productMapper.selectListPage(categoryId, (p - 1) * s, s);
        long total = productMapper.countByCategory(categoryId);
        if (products.isEmpty()) {
            return PageResult.of(total, p, s, Collections.emptyList());
        }
        // 批量查询 SKU 最低价并组装 VO
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        Map<Long, Integer> minPriceMap = productSkuMapper.selectMinPrices(productIds).stream()
                .collect(Collectors.toMap(ProductMinPrice::getProductId, ProductMinPrice::getMinPrice));
        List<ProductVO> list = products.stream()
                .map(product -> ProductVO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .image(firstImage(product.getImages()))
                        .brand(product.getBrand())
                        .categoryId(product.getCategoryId())
                        .minPrice(minPriceMap.get(product.getId()))
                        .sales(product.getSales())
                        .build())
                .collect(Collectors.toList());
        return PageResult.of(total, p, s, list);
    }

    // 查询商品详情
    @Override
    public ProductDetailVO getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        // 游客仅可见上架商品
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        // SKU 列表
        List<ProductSkuVO> skuList = productSkuMapper.selectByProductId(id).stream()
                .map(sku -> ProductSkuVO.builder()
                        .id(sku.getId())
                        .specIds(sku.getSpecIds())
                        .specText(sku.getSpecText())
                        .price(sku.getPrice())
                        .stock(sku.getStock())
                        .build())
                .collect(Collectors.toList());
        // 分类名/店铺名
        String categoryName = null;
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            categoryName = category.getName();
        }
        String shopName = null;
        Merchant merchant = product.getMerchantId() == null ? null : merchantMapper.selectById(product.getMerchantId());
        if (merchant != null) {
            shopName = merchant.getShopName();
        }
        return ProductDetailVO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .images(parseImages(product.getImages()))
                .brand(product.getBrand())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .merchantId(product.getMerchantId())
                .shopName(shopName)
                .status(product.getStatus())
                .sales(product.getSales())
                .skuList(skuList)
                .build();
    }

    // 商家创建商品
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(ProductCreateDTO dto, Long userId) {
        // 校验商家身份与审核状态
        Merchant merchant = requireApprovedMerchant(userId);
        // 校验分类存在
        requireCategory(dto.getCategoryId());
        // 校验 SKU 列表
        validateSkus(dto.getSkuList());
        // 插入商品（默认下架）
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .images(toImagesJson(dto.getImages()))
                .brand(dto.getBrand())
                .categoryId(dto.getCategoryId())
                .merchantId(merchant.getId())
                .status(0)
                .build();
        productMapper.insert(product);
        // 批量插入 SKU
        insertSkus(product.getId(), dto.getSkuList());
        return product.getId();
    }

    // 商家修改商品
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, ProductCreateDTO dto, Long userId) {
        // 校验商家与归属
        Merchant merchant = requireApprovedMerchant(userId);
        Product product = requireOwnership(id, merchant.getId());
        // 分类变更时校验分类存在
        if (dto.getCategoryId() != null) {
            requireCategory(dto.getCategoryId());
        }
        // SKU 列表非空才校验并全量替换
        if (dto.getSkuList() != null && !dto.getSkuList().isEmpty()) {
            validateSkus(dto.getSkuList());
        }
        // 更新基本信息（动态 SQL，null 字段不覆盖）
        productMapper.update(Product.builder()
                .id(id)
                .name(dto.getName())
                .description(dto.getDescription())
                .images(dto.getImages() == null ? null : toImagesJson(dto.getImages()))
                .brand(dto.getBrand())
                .categoryId(dto.getCategoryId())
                .build());
        // 全量替换 SKU
        if (dto.getSkuList() != null && !dto.getSkuList().isEmpty()) {
            productSkuMapper.deleteByProductId(id);
            insertSkus(id, dto.getSkuList());
        }
    }

    // 商家上架/下架
    @Override
    public void updateStatus(Long id, Integer status, Long userId) {
        // 状态值仅允许 0-下架 / 1-上架
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ProductErrorCode.STATUS_ILLEGAL);
        }
        // 校验商家与归属
        Merchant merchant = requireApprovedMerchant(userId);
        requireOwnership(id, merchant.getId());
        productMapper.updateStatus(id, status);
    }

    // 商家删除商品
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id, Long userId) {
        // 校验商家与归属
        Merchant merchant = requireApprovedMerchant(userId);
        requireOwnership(id, merchant.getId());
        try {
            productMapper.deleteById(id);
            productSkuMapper.deleteByProductId(id);
        } catch (DataIntegrityViolationException e) {
            // 商品被购物车/订单引用时外键约束拒绝删除
            log.warn("商品 {} 存在交易引用，删除被拒绝", id);
            throw new BusinessException(ProductErrorCode.PRODUCT_REFERENCED);
        }
    }

    // ================ 私有校验与组装方法 ================

    // 校验当前用户是审核通过的商家
    private Merchant requireApprovedMerchant(Long userId) {
        Merchant merchant = merchantMapper.selectByUserId(userId);
        if (merchant == null) {
            throw new BusinessException(ProductErrorCode.NOT_MERCHANT);
        }
        // merchant.status：0-待审核，1-已通过，2-已拒绝
        if (merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new BusinessException(ProductErrorCode.MERCHANT_NOT_APPROVED);
        }
        return merchant;
    }

    // 校验商品存在且归属当前商家
    private Product requireOwnership(Long productId, Long merchantId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!merchantId.equals(product.getMerchantId())) {
            throw new BusinessException(ProductErrorCode.NO_PERMISSION_OPERATE);
        }
        return product;
    }

    // 校验分类存在
    private void requireCategory(Long categoryId) {
        if (categoryId == null || categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    // 校验 SKU 列表：非空、价格大于 0、库存非负
    private void validateSkus(List<ProductSkuDTO> skuList) {
        if (skuList == null || skuList.isEmpty()) {
            throw new BusinessException(ProductErrorCode.SKU_LIST_EMPTY);
        }
        for (ProductSkuDTO sku : skuList) {
            if (sku.getPrice() == null || sku.getPrice() <= 0) {
                throw new BusinessException(ProductErrorCode.SKU_PRICE_ILLEGAL);
            }
            if (sku.getStock() == null || sku.getStock() < 0) {
                throw new BusinessException(ProductErrorCode.SKU_STOCK_ILLEGAL);
            }
        }
    }

    // 批量插入 SKU
    private void insertSkus(Long productId, List<ProductSkuDTO> skuList) {
        List<ProductSku> skus = skuList.stream()
                .map(sku -> ProductSku.builder()
                        .productId(productId)
                        .specIds(sku.getSpecIds() != null ? sku.getSpecIds() : "")
                        .specText(sku.getSpecText() != null ? sku.getSpecText() : "")
                        .price(sku.getPrice())
                        .stock(sku.getStock())
                        .status(1)
                        .build())
                .collect(Collectors.toList());
        productSkuMapper.insertBatch(skus);
    }

    // images JSON 数组序列化
    private String toImagesJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            return "[]";
        }
    }

    // images JSON 解析，异常返回空列表
    private List<String> parseImages(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(images, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // 取第一张主图
    private String firstImage(String images) {
        List<String> list = parseImages(images);
        return list.isEmpty() ? null : list.get(0);
    }
}
