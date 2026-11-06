package com.ncist.service.impl;

import com.ncist.common.context.UserContext;
import com.ncist.common.enums.AdminErrorCode;
import com.ncist.common.enums.CategoryErrorCode;
import com.ncist.common.enums.CommonErrorCode;
import com.ncist.common.enums.MerchantErrorCode;
import com.ncist.common.enums.UserRoleEnum;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.CategoryUpsertDTO;
import com.ncist.dto.MerchantReviewDTO;
import com.ncist.entity.Category;
import com.ncist.entity.Merchant;
import com.ncist.entity.Product;
import com.ncist.entity.User;
import com.ncist.mapper.CategoryMapper;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.mapper.ProductMapper;
import com.ncist.mapper.UserMapper;
import com.ncist.service.AdminService;
import com.ncist.vo.AdminMerchantVO;
import com.ncist.vo.AdminReportVO;
import com.ncist.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台服务实现（所有方法要求管理员角色）
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

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
                .orderStatusDistribution(distribution)
                .build();
    }

    // ================ 私有辅助 ================

    private void requireAdmin() {
        Integer role = UserContext.getRole();
        if (!UserRoleEnum.ADMIN.getCode().equals(role)) {
            throw new BusinessException(AdminErrorCode.NOT_ADMIN);
        }
    }

    /** 校验父分类存在且层级连续；一级分类强制 parentId=0 */
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

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizeSize(Integer size) {
        return size == null || size < 1 ? 10 : size;
    }

    private Long defaultZero(Long value) {
        return value == null ? 0L : value;
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number num) return num.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private Integer toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number num) return num.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

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
