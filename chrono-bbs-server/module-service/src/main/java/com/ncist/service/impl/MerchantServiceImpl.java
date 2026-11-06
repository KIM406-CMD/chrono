package com.ncist.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.common.enums.MerchantErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.MerchantApplyDTO;
import com.ncist.entity.Address;
import com.ncist.entity.Merchant;
import com.ncist.entity.Order;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.service.MerchantService;
import com.ncist.vo.MerchantInfoVO;
import com.ncist.vo.MerchantOrderVO;
import com.ncist.vo.MerchantStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商家管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(Long userId, MerchantApplyDTO dto) {
        // 同一用户只能申请一次
        Merchant existing = merchantMapper.selectByUserId(userId);
        if (existing != null) {
            throw new BusinessException(MerchantErrorCode.ALREADY_MERCHANT);
        }
        Merchant merchant = Merchant.builder()
                .userId(userId)
                .shopName(dto.getShopName())
                .businessLicense(dto.getBusinessLicense())
                .contactPhone(dto.getContactPhone())
                .status(0) // 待审核
                .build();
        merchantMapper.insertApply(merchant);
    }

    @Override
    public MerchantInfoVO getInfo(Long userId) {
        Merchant merchant = requireMerchant(userId);
        return MerchantInfoVO.builder()
                .id(merchant.getId())
                .userId(merchant.getUserId())
                .shopName(merchant.getShopName())
                .businessLicense(merchant.getBusinessLicense())
                .contactPhone(merchant.getContactPhone())
                .status(merchant.getStatus())
                .reviewReason(merchant.getReviewReason())
                .reviewedAt(merchant.getReviewedAt())
                .createdAt(merchant.getCreatedAt())
                .build();
    }

    @Override
    public PageResult<Map<String, Object>> listProducts(Long userId, Integer status, Integer page, Integer size) {
        Merchant merchant = requireActiveMerchant(userId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = merchantMapper.countMerchantProducts(merchant.getId(), status);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<Map<String, Object>> list = merchantMapper.selectMerchantProducts(
                merchant.getId(), status, (p - 1) * s, s);
        return PageResult.of(total, p, s, list);
    }

    @Override
    public PageResult<MerchantOrderVO> listOrders(Long userId, Integer status, Integer page, Integer size) {
        Merchant merchant = requireActiveMerchant(userId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = merchantMapper.countMerchantOrders(merchant.getId(), status);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<Long> orderIds = merchantMapper.selectMerchantOrderIds(
                merchant.getId(), status, (p - 1) * s, s);
        List<Map<String, Object>> orderRows = merchantMapper.selectMerchantOrders(
                merchant.getId(), orderIds);
        // 批量加载每个订单的商品明细
        Map<Long, List<Map<String, Object>>> itemsByOrder = orderIds.stream()
                .collect(Collectors.toMap(
                        oid -> oid,
                        oid -> merchantMapper.selectMerchantOrderItems(merchant.getId(), oid)));
        List<MerchantOrderVO> list = orderRows.stream()
                .map(row -> toMerchantOrder(row, itemsByOrder.get(toLong(row.get("id")))))
                .toList();
        return PageResult.of(total, p, s, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long userId, Long orderId) {
        Merchant merchant = requireActiveMerchant(userId);
        // 1. 校验订单存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(MerchantErrorCode.ORDER_NOT_BELONG_TO_YOU);
        }
        // 2. 校验订单商品全部属于当前商家
        int notBelongCount = merchantMapper.countOrderItemsNotBelongTo(orderId, merchant.getId());
        if (notBelongCount > 0) {
            throw new BusinessException(MerchantErrorCode.ORDER_NOT_BELONG_TO_YOU);
        }
        // 3. 状态条件更新：已支付 → 已发货
        if (orderMapper.markShipped(orderId) == 0) {
            throw new BusinessException(MerchantErrorCode.SHIPPED_STATUS_ILLEGAL);
        }
    }

    @Override
    public MerchantStatsVO getStats(Long userId, String startDate, String endDate) {
        Merchant merchant = requireActiveMerchant(userId);
        Long totalSales = merchantMapper.sumSales(merchant.getId(), startDate, endDate);
        if (totalSales == null) {
            totalSales = 0L;
        }
        Long orderCount = merchantMapper.countOrders(merchant.getId(), startDate, endDate);
        if (orderCount == null) {
            orderCount = 0L;
        }
        List<MerchantStatsVO.ProductSales> topProducts = merchantMapper
                .selectTopProducts(merchant.getId(), startDate, endDate, 10).stream()
                .map(row -> MerchantStatsVO.ProductSales.builder()
                        .productId(toLong(row.get("productId")))
                        .productName((String) row.get("productName"))
                        .salesCount(toLong(row.get("salesCount")))
                        .totalAmount(toLong(row.get("totalAmount")))
                        .build())
                .toList();
        List<MerchantStatsVO.CategorySales> salesByCategory = merchantMapper
                .selectSalesByCategory(merchant.getId(), startDate, endDate).stream()
                .map(row -> MerchantStatsVO.CategorySales.builder()
                        .categoryId(toLong(row.get("categoryId")))
                        .categoryName((String) row.get("categoryName"))
                        .totalAmount(toLong(row.get("totalAmount")))
                        .build())
                .toList();
        List<MerchantStatsVO.TrendPoint> salesTrend = merchantMapper
                .selectSalesTrend(merchant.getId(), startDate, endDate).stream()
                .map(row -> MerchantStatsVO.TrendPoint.builder()
                        .date(String.valueOf(row.get("date")))
                        .amount(toLong(row.get("amount")))
                        .build())
                .toList();
        return MerchantStatsVO.builder()
                .totalSales(totalSales)
                .orderCount(orderCount)
                .topProducts(topProducts)
                .salesByCategory(salesByCategory)
                .salesTrend(salesTrend)
                .build();
    }

    // ================ 私有辅助 ================

    /** 必须是已审核通过的商家（status=1） */
    private Merchant requireActiveMerchant(Long userId) {
        Merchant m = merchantMapper.selectByUserId(userId);
        if (m == null) {
            throw new BusinessException(MerchantErrorCode.NOT_MERCHANT);
        }
        if (m.getStatus() == null || m.getStatus() != 1) {
            throw new BusinessException(MerchantErrorCode.MERCHANT_STATUS_ILLEGAL);
        }
        return m;
    }

    /** 只要存在商家记录即可（不管审核状态），用于获取商家信息 */
    private Merchant requireMerchant(Long userId) {
        Merchant m = merchantMapper.selectByUserId(userId);
        if (m == null) {
            throw new BusinessException(MerchantErrorCode.NOT_MERCHANT);
        }
        return m;
    }

    /** Map 中取出 Long 值，兼容 Integer/Long/BigDecimal 类型 */
    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number num) {
            return num.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    /** Map 中取出 Integer 值 */
    private Integer toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number num) {
            return num.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private MerchantOrderVO toMerchantOrder(Map<String, Object> row, List<Map<String, Object>> items) {
        MerchantOrderVO.MerchantOrderVOBuilder builder = MerchantOrderVO.builder()
                .id(toLong(row.get("id")))
                .orderNo((String) row.get("orderNo"))
                .userId(toLong(row.get("userId")))
                .totalAmount(toInt(row.get("totalAmount")))
                .status(toInt(row.get("status")))
                .paidAt(toLocalDateTime(row.get("paidAt")))
                .shippedAt(toLocalDateTime(row.get("shippedAt")))
                .createdAt(toLocalDateTime(row.get("createdAt")))
                .itemCount(toInt(row.get("itemCount")));
        // 解析 addressSnapshot
        Address addr = parseAddress((String) row.get("addressSnapshot"));
        if (addr != null) {
            builder.receiverName(addr.getReceiverName())
                    .receiverPhone(addr.getPhone())
                    .receiverAddress(buildAddress(addr));
        }
        // 组装该商家商品明细
        if (items != null && !items.isEmpty()) {
            builder.items(items.stream()
                    .map(it -> MerchantOrderVO.ItemSnapshot.builder()
                            .productId(toLong(it.get("productId")))
                            .productName((String) it.get("productName"))
                            .productImage(firstImage((String) it.get("productImages")))
                            .quantity(toInt(it.get("quantity")))
                            .unitPrice(toInt(it.get("unitPrice")))
                            .subtotal(toInt(it.get("subtotal")))
                            .build())
                    .toList());
        } else {
            builder.items(List.of());
        }
        return builder.build();
    }

    private Address parseAddress(String snapshot) {
        if (snapshot == null || snapshot.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(snapshot, Address.class);
        } catch (Exception e) {
            log.warn("地址快照解析失败: {}", snapshot);
            return null;
        }
    }

    private String buildAddress(Address addr) {
        StringBuilder sb = new StringBuilder();
        if (addr.getProvince() != null) sb.append(addr.getProvince());
        if (addr.getCity() != null) sb.append(addr.getCity());
        if (addr.getDistrict() != null) sb.append(addr.getDistrict());
        if (addr.getDetail() != null) sb.append(" ").append(addr.getDetail());
        return sb.toString().isBlank() ? null : sb.toString();
    }

    private String firstImage(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return null;
        }
        try {
            List<String> list = objectMapper.readValue(imagesJson, new TypeReference<>() {});
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            log.warn("商品图片 JSON 解析失败: {}", imagesJson);
            return null;
        }
    }

    private java.time.LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.time.LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        try {
            return java.time.LocalDateTime.parse(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
