package com.ncist.service.impl;

import com.ncist.common.context.UserContext;
import com.ncist.common.enums.RefundErrorCode;
import com.ncist.common.enums.UserRoleEnum;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.RefundCreateDTO;
import com.ncist.dto.RefundReturnDTO;
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
 * 退款服务实现。
 *
 * <p>退款申请与审核均使用 {@code @Transactional} 保证退款单与订单状态的一致性。
 * 审核通过后订单标记为已退款，拒绝后恢复为原订单状态；
 * 状态条件更新保证同一退款单仅能被审核一次。</p>
 *
 * <p>权限规则：管理员可直接审核；商家必须资质有效且退款商品归属自己。
 * 申请要求订单处于已支付、已发货或已收货状态，且同一订单明细仅能申请一次。</p>
 */
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    /** 允许申请退款的订单状态：2-已支付，3-已发货，4-已收货。 */
    private static final List<Integer> REFUNDABLE_STATUS = List.of(2, 3, 4);

    /** 退款数据访问组件。 */
    private final RefundMapper refundMapper;
    /** 订单数据访问组件，用于状态校验与流转。 */
    private final OrderMapper orderMapper;
    /** 订单明细数据访问组件，用于校验退款来源与金额。 */
    private final OrderItemMapper orderItemMapper;
    /** 商品数据访问组件，用于商家审核时校验商品归属。 */
    private final ProductMapper productMapper;
    /** 商家数据访问组件，用于校验商家审核资质。 */
    private final MerchantMapper merchantMapper;

    /**
     * {@inheritDoc}
     *
     * <p>依次校验订单明细属主、订单状态、退款幂等与金额范围；
     * 申请写入后通过状态条件更新将订单标记为退款中，更新失败不影响申请结果。</p>
     */
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
        Product product = productMapper.selectById(orderItem.getProductId());
        Refund refund = Refund.builder()
                .orderId(orderItem.getOrderId())
                .orderItemId(orderItem.getId())
                .userId(userId)
                .merchantId(product == null ? null : product.getMerchantId())
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

    /** {@inheritDoc} */
    @Override
    public PageResult<RefundVO> myRefunds(Long userId, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = refundMapper.countByUser(userId);
        List<RefundDetail> details = total == 0 ? List.of()
                : refundMapper.selectPageByUser(userId, (p - 1) * s, s);
        return toPage(total, p, s, details);
    }

    /** {@inheritDoc} */
    @Override
    public PageResult<RefundVO> merchantRefunds(Long userId, Integer status, Integer page, Integer size) {
        if (!UserRoleEnum.MERCHANT.getCode().equals(UserContext.getRole())) {
            throw new BusinessException(RefundErrorCode.NO_PERMISSION_REVIEW);
        }
        Merchant merchant = merchantMapper.selectByUserId(userId);
        if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
            throw new BusinessException(RefundErrorCode.NO_PERMISSION_REVIEW);
        }
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = refundMapper.countByMerchant(merchant.getId(), status);
        List<RefundDetail> details = total == 0 ? List.of()
                : refundMapper.selectPageByMerchant(merchant.getId(), status, (p - 1) * s, s);
        return toPage(total, p, s, details);
    }

    /** {@inheritDoc} */
    @Override
    public PageResult<RefundVO> adminRefunds(Long userId, Integer status, Integer page, Integer size) {
        if (!UserRoleEnum.ADMIN.getCode().equals(UserContext.getRole())) {
            throw new BusinessException(RefundErrorCode.NO_PERMISSION_REVIEW);
        }
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = refundMapper.countAll(status);
        List<RefundDetail> details = total == 0 ? List.of()
                : refundMapper.selectPageAll(status, (p - 1) * s, s);
        return toPage(total, p, s, details);
    }

    /** {@inheritDoc} */
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

    /**
     * {@inheritDoc}
     *
     * <p>先做角色/资质/商品归属校验，再通过状态条件更新审核结果；
     * 通过时订单标记为已退款，拒绝时恢复为原状态，均在同一事务中完成。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long userId, Long id, RefundReviewDTO dto) {
        RefundDetail refund = refundMapper.selectDetailById(id);
        if (refund == null) {
            throw new BusinessException(RefundErrorCode.REFUND_NOT_FOUND);
        }
        // 1. 权限：管理员直接放行；商家需资质有效且退款商品归属自己
        requireReviewPermission(userId, refund);
        // 2. 审核落库（状态条件更新保证只审一次）
        boolean approved = Integer.valueOf(1).equals(dto.getApproved());
        boolean isReturn = Integer.valueOf(2).equals(refund.getType());
        int targetStatus = approved ? (isReturn ? 3 : 1) : 2;
        if (refundMapper.markReviewed(id, targetStatus, userId, dto.getReviewReason()) == 0) {
            throw new BusinessException(RefundErrorCode.ALREADY_REVIEWED);
        }
        // 3. 同步订单状态：仅退款通过/拒绝时流转；退货通过后等待买家寄回
        if (approved) {
            if (!isReturn) {
                orderMapper.markRefunded(refund.getOrderId());
            }
        } else {
            orderMapper.revertRefunding(refund.getOrderId());
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReturn(Long userId, Long id, RefundReturnDTO dto) {
        RefundDetail refund = refundMapper.selectDetailById(id);
        if (refund == null) {
            throw new BusinessException(RefundErrorCode.REFUND_NOT_FOUND);
        }
        if (!refund.getUserId().equals(userId)) {
            throw new BusinessException(RefundErrorCode.NO_PERMISSION_OPERATE);
        }
        if (!Integer.valueOf(2).equals(refund.getType())
                || !Integer.valueOf(3).equals(refund.getStatus())) {
            throw new BusinessException(RefundErrorCode.RETURN_STATUS_ILLEGAL);
        }
        if (dto.getReturnCompany() == null || dto.getReturnCompany().isBlank()
                || dto.getReturnLogisticsNo() == null || dto.getReturnLogisticsNo().isBlank()) {
            throw new BusinessException(RefundErrorCode.RETURN_INFO_INVALID);
        }
        if (refundMapper.markReturned(id, userId, dto.getReturnCompany(), dto.getReturnLogisticsNo()) == 0) {
            throw new BusinessException(RefundErrorCode.RETURN_STATUS_ILLEGAL);
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturnReceived(Long userId, Long id) {
        RefundDetail refund = refundMapper.selectDetailById(id);
        if (refund == null) {
            throw new BusinessException(RefundErrorCode.REFUND_NOT_FOUND);
        }
        requireReviewPermission(userId, refund);
        if (!Integer.valueOf(4).equals(refund.getStatus())) {
            throw new BusinessException(RefundErrorCode.RETURN_STATUS_ILLEGAL);
        }
        if (refundMapper.markReturnReceived(id) == 0) {
            throw new BusinessException(RefundErrorCode.RETURN_STATUS_ILLEGAL);
        }
        orderMapper.markRefunded(refund.getOrderId());
    }

    /**
     * 校验审核/确认收货权限：管理员直接放行；商家需资质有效且商品归属自己。
     *
     * @param userId 操作人用户 ID
     * @param refund 退款单明细
     * @throws BusinessException 无审核权限时抛出
     */
    private void requireReviewPermission(Long userId, RefundDetail refund) {
        if (UserRoleEnum.ADMIN.getCode().equals(com.ncist.common.context.UserContext.getRole())) {
            return;
        }
        Merchant merchant = merchantMapper.selectByUserId(userId);
        if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
            throw new BusinessException(RefundErrorCode.NO_PERMISSION_REVIEW);
        }
        Product product = productMapper.selectById(refund.getProductId());
        if (product == null || !merchant.getId().equals(product.getMerchantId())) {
            throw new BusinessException(RefundErrorCode.NO_PERMISSION_REVIEW);
        }
    }

    /**
     * 组装退款分页结果。
     *
     * @param total   总记录数
     * @param page    页码
     * @param size    每页条数
     * @param details 当前页退款明细
     * @return 退款分页结果
     */
    private PageResult<RefundVO> toPage(long total, int page, int size, List<RefundDetail> details) {
        return PageResult.of(total, page, size, details.stream().map(this::toVO).toList());
    }

    /**
     * 将退款联查明细转换为视图对象。
     *
     * @param detail 退款联查明细
     * @return 退款视图对象
     */
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
                .merchantId(detail.getMerchantId())
                .reviewerId(detail.getReviewerId())
                .reviewReason(detail.getReviewReason())
                .reviewedAt(detail.getReviewedAt())
                .returnCompany(detail.getReturnCompany())
                .returnLogisticsNo(detail.getReturnLogisticsNo())
                .returnedAt(detail.getReturnedAt())
                .returnReceivedAt(detail.getReturnReceivedAt())
                .createdAt(detail.getCreatedAt())
                .build();
    }
}
