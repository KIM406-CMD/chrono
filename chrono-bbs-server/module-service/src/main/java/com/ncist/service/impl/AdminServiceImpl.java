package com.ncist.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.context.UserContext;
import com.ncist.common.enums.AdminErrorCode;
import com.ncist.common.enums.CategoryErrorCode;
import com.ncist.common.enums.CommonErrorCode;
import com.ncist.common.enums.MerchantErrorCode;
import com.ncist.common.enums.ProductErrorCode;
import com.ncist.common.enums.UserRoleEnum;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.CategoryUpsertDTO;
import com.ncist.dto.MerchantReviewDTO;
import com.ncist.dto.ProductCreateDTO;
import com.ncist.dto.ProductSkuDTO;
import com.ncist.entity.Category;
import com.ncist.entity.Merchant;
import com.ncist.entity.Product;
import com.ncist.entity.ProductSku;
import com.ncist.entity.User;
import com.ncist.mapper.CategoryMapper;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.mapper.ProductMapper;
import com.ncist.mapper.ProductSkuMapper;
import com.ncist.mapper.UserMapper;
import com.ncist.service.AdminService;
import com.ncist.service.ProductSearchIndexService;
import com.ncist.vo.AdminMerchantVO;
import com.ncist.vo.AdminReportVO;
import com.ncist.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台服务实现（所有方法要求管理员角色）。
 *
 * <p>每个 public 方法入口均通过 {@code UserContext} 校验管理员角色。
 * 商家审核在事务中完成“更新审核状态 + 升级用户角色”；
 * 其余写操作多为单表单语句。分页参数在内部统一兜底，页码至少为 1、每页默认 10 条。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    /** 用户数据访问组件。 */
    private final UserMapper userMapper;
    /** 商家数据访问组件。 */
    private final MerchantMapper merchantMapper;
    /** 分类数据访问组件。 */
    private final CategoryMapper categoryMapper;
    /** 商品数据访问组件。 */
    private final ProductMapper productMapper;
    /** 商品 SKU 数据访问组件，用于管理员改品时替换 SKU。 */
    private final ProductSkuMapper productSkuMapper;
    /** 订单数据访问组件，用于订单列表与销售报表。 */
    private final OrderMapper orderMapper;
    /** JSON 序列化组件，用于商品图片列表与 JSON 字符串互转。 */
    private final ObjectMapper objectMapper;
    /** 商品搜索索引服务，管理员上下架/删除商品后同步 Elasticsearch。 */
    private final ProductSearchIndexService productSearchIndexService;

    /** {@inheritDoc} */
    @Override
    public PageResult<AdminUserVO> listUsers(String keyword, Integer role, Integer status, Integer page, Integer size) {
        requireAdmin();
        int p = normalizePage(page);
        int s = normalizeSize(size);
        long total = userMapper.countAdmin(keyword, role, status);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<AdminUserVO> list = userMapper.selectAdminPage(keyword, role, status, (p - 1) * s, s).stream()
                .map(u -> AdminUserVO.builder()
                        .id(u.getId()).username(u.getUsername()).nickname(u.getNickname())
                        .phone(u.getPhone()).email(u.getEmail()).role(u.getRole())
                        .status(u.getStatus()).createdAt(u.getCreatedAt())
                        .build())
                .toList();
        return PageResult.of(total, p, s, list);
    }

    /**
     * {@inheritDoc}
     *
     * <p>先校验商品存在与分类有效，再动态更新基础信息；
     * SKU 列表非空时在同一事务中全量替换。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, ProductCreateDTO dto) {
        requireAdmin();
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        if (dto.getCategoryId() != null && categoryMapper.selectById(dto.getCategoryId()) == null) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
        if (dto.getSkuList() != null && !dto.getSkuList().isEmpty()) {
            validateSkus(dto.getSkuList());
        }
        productMapper.update(Product.builder()
                .id(id)
                .name(dto.getName())
                .description(dto.getDescription())
                .images(dto.getImages() == null ? null : toImagesJson(dto.getImages()))
                .brand(dto.getBrand())
                .categoryId(dto.getCategoryId())
                .build());
        if (dto.getSkuList() != null && !dto.getSkuList().isEmpty()) {
            productSkuMapper.deleteByProductId(id);
            List<ProductSku> skus = dto.getSkuList().stream()
                    .map(sku -> ProductSku.builder()
                            .productId(id)
                            .specIds(sku.getSpecIds())
                            .specText(sku.getSpecText())
                            .price(sku.getPrice())
                            .stock(sku.getStock())
                            .build())
                    .toList();
            productSkuMapper.insertBatch(skus);
        }
        // 商品名称/价格等可能变化，同步搜索索引
        productSearchIndexService.syncProduct(id);
    }

    /** {@inheritDoc} */
    @Override
    public void updateProductStatus(Long id, Integer status) {
        requireAdmin();
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ProductErrorCode.STATUS_ILLEGAL);
        }
        if (productMapper.selectById(id) == null) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        productMapper.updateStatus(id, status);
        productSearchIndexService.syncProduct(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p>删除失败由外键约束触发时转换为“商品已被引用”的业务异常并回滚事务。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        requireAdmin();
        if (productMapper.selectById(id) == null) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        // 数据库未建立外键约束，删除前显式校验交易引用
        if (productMapper.countCartReferences(id) > 0 || productMapper.countOrderReferences(id) > 0) {
            throw new BusinessException(ProductErrorCode.PRODUCT_REFERENCED);
        }
        try {
            productMapper.deleteById(id);
            productSkuMapper.deleteByProductId(id);
        } catch (DataIntegrityViolationException e) {
            log.warn("商品 {} 存在交易引用，管理员删除被拒绝", id);
            throw new BusinessException(ProductErrorCode.PRODUCT_REFERENCED);
        }
        productSearchIndexService.deleteProduct(id);
    }

    /**
     * 校验 SKU 价格与库存。
     *
     * @param skus SKU 列表
     * @throws BusinessException 价格不大于 0 或库存为负时抛出
     */
    private void validateSkus(List<ProductSkuDTO> skus) {
        for (ProductSkuDTO sku : skus) {
            if (sku.getPrice() == null || sku.getPrice() <= 0
                    || sku.getStock() == null || sku.getStock() < 0) {
                throw new BusinessException(CommonErrorCode.BAD_REQUEST);
            }
        }
    }

    /**
     * 将图片 URL 列表序列化为 JSON 字符串。
     *
     * @param images 图片 URL 列表
     * @return JSON 字符串；列表为空时返回 null
     */
    private String toImagesJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>管理员账号不允许被禁用，避免后台失去管理入口。</p>
     */
    @Override
    public void updateUserStatus(Long id, Integer status) {
        requireAdmin();
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
        User target = userMapper.selectById(id);
        if (target == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }
        if (UserRoleEnum.ADMIN.getCode().equals(target.getRole())) {
            throw new BusinessException(AdminErrorCode.CANNOT_DISABLE_ADMIN);
        }
        userMapper.updateStatus(id, status);
    }

    /** {@inheritDoc} */
    @Override
    public PageResult<AdminMerchantVO> listMerchants(Integer status, Integer page, Integer size) {
        requireAdmin();
        int p = normalizePage(page);
        int s = normalizeSize(size);
        long total = merchantMapper.countByStatus(status);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<AdminMerchantVO> list = merchantMapper.selectByStatusPage(status, (p - 1) * s, s).stream()
                .map(row -> AdminMerchantVO.builder()
                        .id(toLong(row.get("id")))
                        .userId(toLong(row.get("userId")))
                        .applicantUsername((String) row.get("applicantUsername"))
                        .shopName((String) row.get("shopName"))
                        .contactPhone((String) row.get("contactPhone"))
                        .status(toInt(row.get("status")))
                        .reviewReason((String) row.get("reviewReason"))
                        .reviewedAt(toLocalDateTime(row.get("reviewedAt")))
                        .createdAt(toLocalDateTime(row.get("createdAt")))
                        .build())
                .toList();
        return PageResult.of(total, p, s, list);
    }

    /**
     * {@inheritDoc}
     *
     * <p>使用“status=0”条件更新保证同一申请不会被重复审核；
     * 审核通过后同步升级申请人角色，两个写操作在同一事务内完成。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewMerchant(Long id, MerchantReviewDTO dto) {
        requireAdmin();
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(MerchantErrorCode.MERCHANT_NOT_FOUND);
        }
        int newStatus = dto.getApproved() != null && dto.getApproved() == 1 ? 1 : 2;
        // status=0 条件保证不重复审核
        if (merchantMapper.updateReview(id, newStatus, dto.getReviewReason()) == 0) {
            throw new BusinessException(AdminErrorCode.MERCHANT_ALREADY_REVIEWED);
        }
        // 审核通过：申请人角色升级为商家
        if (newStatus == 1) {
            userMapper.updateRole(merchant.getUserId(), UserRoleEnum.MERCHANT.getCode());
        }
    }

    /** {@inheritDoc} */
    @Override
    public Long createCategory(CategoryUpsertDTO dto) {
        requireAdmin();
        if (dto.getName() == null || dto.getName().isBlank() || dto.getLevel() == null
                || dto.getLevel() < 1 || dto.getLevel() > 3) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
        Long parentId = resolveParent(dto.getParentId(), dto.getLevel());
        Category category = Category.builder()
                .name(dto.getName())
                .parentId(parentId)
                .level(dto.getLevel())
                .sortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder())
                .icon(dto.getIcon())
                .build();
        categoryMapper.insert(category);
        return category.getId();
    }

    /** {@inheritDoc} */
    @Override
    public void updateCategory(Long id, CategoryUpsertDTO dto) {
        requireAdmin();
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
        // 三个可更新字段全为空时 <set> 会渲染为空导致 SQL 语法错误，提前拦截
        if (dto.getName() == null && dto.getSortOrder() == null && dto.getIcon() == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
        Category category = Category.builder()
                .id(id).name(dto.getName()).sortOrder(dto.getSortOrder()).icon(dto.getIcon())
                .build();
        categoryMapper.update(category);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteCategory(Long id) {
        requireAdmin();
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
        if (categoryMapper.countChildren(id) > 0) {
            throw new BusinessException(AdminErrorCode.CATEGORY_HAS_CHILDREN);
        }
        if (productMapper.countByCategoryId(id) > 0) {
            throw new BusinessException(AdminErrorCode.CATEGORY_HAS_PRODUCTS);
        }
        categoryMapper.deleteById(id);
    }

    /** {@inheritDoc} */
    @Override
    public PageResult<Map<String, Object>> listProducts(String keyword, Long categoryId, Long merchantId,
                                                         Integer status, Integer page, Integer size) {
        requireAdmin();
        int p = normalizePage(page);
        int s = normalizeSize(size);
        long total = productMapper.countAdmin(keyword, categoryId, merchantId, status);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<Map<String, Object>> list = productMapper
                .selectAdminPage(keyword, categoryId, merchantId, status, (p - 1) * s, s).stream()
                .map(this::productToMap)
                .toList();
        return PageResult.of(total, p, s, list);
    }

    /** {@inheritDoc} */
    @Override
    public PageResult<Map<String, Object>> listOrders(String orderNo, Integer status, Integer page, Integer size) {
        requireAdmin();
        int p = normalizePage(page);
        int s = normalizeSize(size);
        long total = orderMapper.countAdmin(orderNo, status);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<Map<String, Object>> list = orderMapper.selectAdminPage(orderNo, status, (p - 1) * s, s);
        return PageResult.of(total, p, s, list);
    }

    /**
     * {@inheritDoc}
     *
     * <p>销售趋势按 groupBy 分组统计；订单状态分布固定包含待支付、已支付、已发货、
     * 已完成与已取消五种状态，缺失状态补 0。</p>
     */
    @Override
    public AdminReportVO getReport(String startDate, String endDate, String groupBy) {
        requireAdmin();
        String group = groupBy == null || groupBy.isBlank() ? "day" : groupBy;
        Long totalSales = defaultZero(orderMapper.sumSalesGlobal(startDate, endDate));
        Long totalOrders = defaultZero(orderMapper.countOrdersGlobal(startDate, endDate));
        Long avgOrderAmount = totalOrders > 0 ? totalSales / totalOrders : 0L;

        List<AdminReportVO.ProductSales> topProducts = orderMapper
                .selectTopProductsGlobal(startDate, endDate, 10).stream()
                .map(row -> AdminReportVO.ProductSales.builder()
                        .productId(toLong(row.get("productId")))
                        .productName((String) row.get("productName"))
                        .salesCount(toLong(row.get("salesCount")))
                        .totalAmount(toLong(row.get("totalAmount")))
                        .build())
                .toList();
        List<AdminReportVO.CategorySales> salesByCategory = orderMapper
                .selectSalesByCategoryGlobal(startDate, endDate).stream()
                .map(row -> AdminReportVO.CategorySales.builder()
                        .categoryId(toLong(row.get("categoryId")))
                        .categoryName((String) row.get("categoryName"))
                        .totalAmount(toLong(row.get("totalAmount")))
                        .build())
                .toList();
        List<AdminReportVO.MerchantSales> salesByMerchant = orderMapper
                .selectSalesByMerchantGlobal(startDate, endDate).stream()
                .map(row -> AdminReportVO.MerchantSales.builder()
                        .merchantId(toLong(row.get("merchantId")))
                        .shopName((String) row.get("shopName"))
                        .totalAmount(toLong(row.get("totalAmount")))
                        .build())
                .toList();
        List<AdminReportVO.TrendPoint> salesTrend = orderMapper
                .selectSalesTrendGlobal(startDate, endDate, group).stream()
                .map(row -> AdminReportVO.TrendPoint.builder()
                        .date(String.valueOf(row.get("date")))
                        .amount(toLong(row.get("amount")))
                        .build())
                .toList();
        List<AdminReportVO.OrderTrendPoint> orderTrend = orderMapper
                .selectOrderTrendGlobal(startDate, endDate).stream()
                .map(row -> AdminReportVO.OrderTrendPoint.builder()
                        .date(String.valueOf(row.get("date")))
                        .createdCount(toLong(row.get("createdCount")))
                        .paidCount(toLong(row.get("paidCount")))
                        .completedCount(toLong(row.get("completedCount")))
                        .build())
                .toList();
        // 订单状态分布：1=pending 2=paid 3=shipped 4=completed 5=cancelled
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("pending", 0L);
        distribution.put("paid", 0L);
        distribution.put("shipped", 0L);
        distribution.put("completed", 0L);
        distribution.put("cancelled", 0L);
        for (Map<String, Object> row : orderMapper.selectStatusDistribution()) {
            String key = switch (toInt(row.get("status"))) {
                case 1 -> "pending";
                case 2 -> "paid";
                case 3 -> "shipped";
                case 4 -> "completed";
                case 5 -> "cancelled";
                default -> null;
            };
            if (key != null) {
                distribution.put(key, toLong(row.get("cnt")));
            }
        }
        return AdminReportVO.builder()
                .totalSales(totalSales)
                .totalOrders(totalOrders)
                .avgOrderAmount(avgOrderAmount)
                .topProducts(topProducts)
                .salesByCategory(salesByCategory)
                .salesByMerchant(salesByMerchant)
                .salesTrend(salesTrend)
                .orderTrend(orderTrend)
                .orderStatusDistribution(distribution)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    public int rebuildSearchIndex() {
        requireAdmin();
        try {
            return productSearchIndexService.syncAll();
        } catch (Exception e) {
            log.error("全量重建搜索索引失败：{}", e.getMessage());
            throw new BusinessException(CommonErrorCode.INTERNAL_ERROR);
        }
    }

    // ================ 私有辅助 ================

    /**
     * 校验当前登录用户为管理员。
     *
     * @throws BusinessException 当前用户角色不是管理员时抛出
     */
    private void requireAdmin() {
        Integer role = UserContext.getRole();
        if (!UserRoleEnum.ADMIN.getCode().equals(role)) {
            throw new BusinessException(AdminErrorCode.NOT_ADMIN);
        }
    }

    /**
     * 解析并校验父分类：一级分类强制 parentId=0；
     * 非一级分类要求父分类存在且层级连续（父层级 = 当前层级 - 1）。
     *
     * @param parentId 请求中的父分类 ID
     * @param level    当前分类层级（1~3）
     * @return 实际使用的父分类 ID
     * @throws BusinessException 父分类缺失、不存在或层级不连续时抛出
     */
    private Long resolveParent(Long parentId, Integer level) {
        if (level == 1) {
            return 0L;
        }
        if (parentId == null || parentId == 0) {
            throw new BusinessException(AdminErrorCode.CATEGORY_PARENT_INVALID);
        }
        Category parent = categoryMapper.selectById(parentId);
        if (parent == null || parent.getLevel() == null || parent.getLevel() != level - 1) {
            throw new BusinessException(AdminErrorCode.CATEGORY_PARENT_INVALID);
        }
        return parentId;
    }

    /**
     * 归一化页码：为空或小于 1 时按第 1 页处理。
     *
     * @param page 原始页码
     * @return 合法页码
     */
    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    /**
     * 归一化每页条数：为空或小于 1 时按 10 条处理。
     *
     * @param size 原始每页条数
     * @return 合法每页条数
     */
    private int normalizeSize(Integer size) {
        return size == null || size < 1 ? 10 : size;
    }

    /**
     * 将 null 数值转换为 0，避免报表计算空指针。
     *
     * @param value 原始数值
     * @return 非 null 数值
     */
    private Long defaultZero(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * 将 Map 查询结果中的数值转换为 Long。
     *
     * @param value 原始值，可为 Number 或可解析字符串
     * @return 转换结果；value 为 null 时返回 0
     */
    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number num) return num.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    /**
     * 将 Map 查询结果中的数值转换为 Integer。
     *
     * @param value 原始值，可为 Number 或可解析字符串
     * @return 转换结果；value 为 null 时返回 0
     */
    private Integer toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number num) return num.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    /**
     * 将 Map 查询结果中的时间值转换为 {@link java.time.LocalDateTime}。
     *
     * @param value 原始时间值，可能为 LocalDateTime、Timestamp 或可解析字符串
     * @return 转换后的时间；value 为空或解析失败时返回 null
     */
    private java.time.LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDateTime ldt) return ldt;
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        try {
            return java.time.LocalDateTime.parse(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将商品实体转换为管理后台展示用的有序 Map。
     *
     * @param p 商品实体
     * @return 按固定字段顺序组织的商品信息
     */
    private Map<String, Object> productToMap(Product p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("description", p.getDescription());
        map.put("images", p.getImages());
        map.put("brand", p.getBrand());
        map.put("categoryId", p.getCategoryId());
        map.put("merchantId", p.getMerchantId());
        map.put("status", p.getStatus());
        map.put("sales", p.getSales());
        map.put("createdAt", p.getCreatedAt());
        map.put("updatedAt", p.getUpdatedAt());
        return map;
    }
}