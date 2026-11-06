package com.ncist.service.impl;

import com.ncist.common.enums.RefundErrorCode;
import com.ncist.common.enums.UserRoleEnum;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.RefundCreateDTO;
import com.ncist.dto.RefundReviewDTO;
import com.ncist.entity.Merchant;
import com.ncist.entity.Order;
import com.ncist.entity.OrderItem;
import com.ncist.entity.Product;
import com.ncist.entity.Refund;
import com.ncist.entity.RefundDetail;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.OrderItemMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.mapper.ProductMapper;
import com.ncist.mapper.RefundMapper;
import com.ncist.service.RefundService;
import com.ncist.vo.RefundVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 退款服务实现
 */
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    /** 已支付/已发货/已收货可申请退款 */
    private static final List<Integer> REFUNDABLE_STATUS = List.of(2, 3, 4);

    private final RefundMapper refundMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(Long userId, RefundCreateDTO dto) {
        // 1. 校验订单明细属主
        OrderItem orderItem = orderItemMapper.selectById(dto.getOrderItemId());
        if (orderItem == null) {
            throw new BusinessException(RefundErrorCode.ORDER_ITEM_NOT_FOUND);
        }
        Order order = orderMapper.selectById(orderItem.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(RefundErrorCode.NO_PERMISSION_OPERATE);
        }
        // 2. 订单状态：已支付/已发货/已收货可退款
        if (order.getStatus() == null || !REFUNDABLE_STATUS.contains(order.getStatus())) {
            throw new BusinessException(RefundErrorCode.ORDER_NOT_REFUNDABLE);
        }
        // 3. 幂等：一个订单明细仅一条退款申请
        if (refundMapper.existsByOrderItemId(orderItem.getId()) > 0) {
            throw new BusinessException(RefundErrorCode.ALREADY_REFUNDING);
        }
        // 4. 金额校验：大于 0 且不超过明细小计
        if (dto.getAmount() == null || dto.getAmount() <= 0 || dto.getAmount() > orderItem.getSubtotal()) {
            throw new BusinessException(RefundErrorCode.AMOUNT_INVALID);
        }
        Refund refund = Refund.builder()
                .orderId(orderItem.getOrderId())
                .orderItemId(orderItem.getId())
                .userId(userId)
                .type(dto.getType())
                .reason(dto.getReason())
                .amount(dto.getAmount())
                .status(0)
                .build();
        refundMapper.insert(refund);
        // 5. 订单进入退款中（状态条件更新，失败不影响申请）
        orderMapper.markRefunding(orderItem.getOrderId());
        return refund.getId();
    }

    @Override
    public PageResult<RefundVO> myRefunds(Long userId, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = refundMapper.countByUser(userId);
        List<RefundDetail> details = total == 0 ? List.of()
                : refundMapper.selectPageByUser(userId, (p - 1) * s, s);
        return PageResult.of(total, p, s, details.stream().map(this::toVO).toList());
    }

    @Override
    public RefundVO detail(Long userId, Long id) {
        RefundDetail detail = refundMapper.selectDetailById(id);
        if (detail == null) {
            throw new BusinessException(RefundErrorCode.REFUND_NOT_FOUND);
        }
        if (!detail.getUserId().equals(userId)) {
            throw new BusinessException(RefundErrorCode.NO_PERMISSION_OPERATE);
        }
        return toVO(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long userId, Long id, RefundReviewDTO dto) {
        RefundDetail refund = refundMapper.selectDetailById(id);
        if (refund == null) {
            throw new BusinessException(RefundErrorCode.REFUND_NOT_FOUND);
        }
        // 1. 权限：管理员直接放行；商家需资质有效且退款商品归属自己
        if (!UserRoleEnum.ADMIN.getCode().equals(com.ncist.common.context.UserContext.getRole())) {
            Merchant merchant = merchantMapper.selectByUserId(userId);
            if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
                throw new BusinessException(RefundErrorCode.NO_PERMISSION_REVIEW);
            }
            Product product = productMapper.selectById(refund.getProductId());
            if (product == null || !merchant.getId().equals(product.getMerchantId())) {
                throw new BusinessException(RefundErrorCode.NO_PERMISSION_REVIEW);
            }
        }
        // 2. 审核落库（状态条件更新保证只审一次）
        boolean approved = Integer.valueOf(1).equals(dto.getApproved());
        if (refundMapper.markReviewed(id, approved ? 1 : 2, userId, dto.getReviewReason()) == 0) {
            throw new BusinessException(RefundErrorCode.ALREADY_REVIEWED);
        }
        // 3. 同步订单状态：通过 → 已退款；拒绝 → 恢复已收货
        if (approved) {
            orderMapper.markRefunded(refund.getOrderId());
        } else {
            orderMapper.revertRefunding(refund.getOrderId());
        }
    }

    private RefundVO toVO(RefundDetail detail) {
        return RefundVO.builder()
                .id(detail.getId())
                .orderId(detail.getOrderId())
                .orderItemId(detail.getOrderItemId())
                .userId(detail.getUserId())
                .productId(detail.getProductId())
                .productName(detail.getProductName())
                .type(detail.getType())
                .reason(detail.getReason())
                .amount(detail.getAmount())
                .status(detail.getStatus())
                .reviewerId(detail.getReviewerId())
                .reviewReason(detail.getReviewReason())
                .reviewedAt(detail.getReviewedAt())
                .createdAt(detail.getCreatedAt())
                .build();
    }
}
