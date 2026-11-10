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
import com.ncist.service.ProductSearchIndexService;
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
 * 商品服务实现。
 *
 * <p>商品与 SKU 分表存储，商品图片以 JSON 数组保存。创建、修改与删除涉及
 * 商品主表与 SKU 子表的多表写操作，使用 {@code @Transactional} 保证一致性；
 * 修改时 SKU 采用全量替换策略。</p>
 *
 * <p>商品被购物车或订单引用时，数据库外键约束会拒绝删除，实现类将其转换为
 * “商品已被引用”的业务异常。列表查询会批量获取 SKU 最低价，避免逐商品查询。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    /** 商品数据访问组件。 */
    private final ProductMapper productMapper;
    /** SKU 数据访问组件。 */
    private final ProductSkuMapper productSkuMapper;
    /** 分类数据访问组件，用于校验分类存在。 */
    private final CategoryMapper categoryMapper;
    /** 商家数据访问组件，用于校验商家资质与商品归属。 */
    private final MerchantMapper merchantMapper;
    /** JSON 序列化组件，用于商品图片数组编解码。 */
    private final ObjectMapper objectMapper;
    private final ProductSearchIndexService productSearchIndexService;

    /**
     * {@inheritDoc}
     *
     * <p>分页参数兜底：页码至少为 1，每页默认 10 条、上限 100 条；
     * 批量查询 SKU 最低价后组装列表。</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>仅上架商品对外可见；分类或商家已被删除时对应名称为空，不影响商品详情返回。</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>商品初始状态为下架（0），商品主表与 SKU 子表在同一事务中写入。</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>SKU 列表非空时先删除原 SKU 再批量插入，主表更新与 SKU 替换在同一事务中完成。</p>
     */
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
        // 同步搜索索引（失败只记录日志，不回滚商品主业务）
        productSearchIndexService.syncProduct(id);
    }

    /** {@inheritDoc} */
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
        // 上下架后同步搜索索引：上架写入、下架移除
        productSearchIndexService.syncProduct(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p>商品与 SKU 的删除在同一事务中执行；外键约束异常会被转换为
     * “商品已被引用”的业务异常，从而触发事务回滚。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id, Long userId) {
        // 校验商家与归属
        Merchant merchant = requireApprovedMerchant(userId);
        requireOwnership(id, merchant.getId());
        // 数据库未建立外键约束，删除前显式校验交易引用
        if (productMapper.countCartReferences(id) > 0 || productMapper.countOrderReferences(id) > 0) {
            throw new BusinessException(ProductErrorCode.PRODUCT_REFERENCED);
        }
        try {
            productMapper.deleteById(id);
            productSkuMapper.deleteByProductId(id);
        } catch (DataIntegrityViolationException e) {
            // 兜底：未来若补充外键约束，仍由约束拒绝删除
            log.warn("商品 {} 存在交易引用，删除被拒绝", id);
            throw new BusinessException(ProductErrorCode.PRODUCT_REFERENCED);
        }
        // 删除成功后移除搜索索引
        productSearchIndexService.deleteProduct(id);
    }

    // ================ 私有校验与组装方法 ================

    /**
     * 校验当前用户是审核通过（status=1）的商家。
     *
     * @param userId 用户 ID
     * @return 商家实体
     * @throws BusinessException 用户非商家或商家审核未通过时抛出
     */
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

    /**
     * 校验商品存在且归属指定商家。
     *
     * @param productId  商品 ID
     * @param merchantId 当前商家 ID
     * @return 商品实体
     * @throws BusinessException 商品不存在或不属于该商家时抛出
     */
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

    /**
     * 校验分类存在。
     *
     * @param categoryId 分类 ID
     * @throws BusinessException 分类 ID 为空或分类不存在时抛出
     */
    private void requireCategory(Long categoryId) {
        if (categoryId == null || categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    /**
     * 校验 SKU 列表：非空、价格大于 0、库存非负。
     *
     * @param skuList SKU 参数列表
     * @throws BusinessException 列表为空、价格非法或库存为负时抛出
     */
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

    /**
     * 将 SKU 参数转换为实体并批量插入，规格字段为空时写入空字符串。
     *
     * @param productId 商品 ID
     * @param skuList   SKU 参数列表
     */
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

    /**
     * 将图片列表序列化为 JSON 数组。
     *
     * @param images 图片 URL 列表
     * @return JSON 数组字符串；列表为空或序列化失败时返回 {@code []}
     */
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

    /**
     * 解析商品图片 JSON 数组。
     *
     * @param images 图片 JSON 字符串
     * @return 图片 URL 列表；数据为空或解析失败时返回空列表
     */
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

    /**
     * 取商品图片列表中的第一张作为主图。
     *
     * @param images 图片 JSON 字符串
     * @return 首张图片 URL；无图片时返回 null
     */
    private String firstImage(String images) {
        List<String> list = parseImages(images);
        return list.isEmpty() ? null : list.get(0);
    }
}
