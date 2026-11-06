package com.ncist.service.impl;

import com.ncist.common.enums.CommentErrorCode;
import com.ncist.common.enums.UserRoleEnum;
import com.ncist.common.context.UserContext;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.dto.CommentAppendDTO;
import com.ncist.dto.CommentCreateDTO;
import com.ncist.dto.CommentReplyDTO;
import com.ncist.entity.Comment;
import com.ncist.entity.CommentDetail;
import com.ncist.entity.Merchant;
import com.ncist.entity.Order;
import com.ncist.entity.OrderItem;
import com.ncist.entity.Product;
import com.ncist.mapper.CommentMapper;
import com.ncist.mapper.MerchantMapper;
import com.ncist.mapper.OrderItemMapper;
import com.ncist.mapper.OrderMapper;
import com.ncist.mapper.ProductMapper;
import com.ncist.service.CommentService;
import com.ncist.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    /** 已收货/已完成 */
    private static final int ORDER_STATUS_RECEIVED = 4;

    private final CommentMapper commentMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;

    @Override
    public PageResult<CommentVO> listByProduct(Long productId, Integer star, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        long total = commentMapper.countFirst(productId, star);
        if (total == 0) {
            return PageResult.of(0L, p, s, List.of());
        }
        List<CommentDetail> firstPages = commentMapper.selectFirstPage(productId, star, (p - 1) * s, s);
        // 批量查子评论（追评/回复），按父评论分组
        List<Long> parentIds = firstPages.stream().map(CommentDetail::getId).toList();
        Map<Long, List<CommentDetail>> childrenByParent = commentMapper.selectChildrenByParentIds(parentIds)
                .stream().collect(Collectors.groupingBy(CommentDetail::getParentId));
        List<CommentVO> list = firstPages.stream()
                .map(comment -> toVO(comment, childrenByParent.getOrDefault(comment.getId(), List.of())))
                .toList();
        return PageResult.of(total, p, s, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, CommentCreateDTO dto) {
        // 1. 校验订单明细属主
        OrderItem orderItem = requireOwnedOrderItem(dto.getOrderItemId(), userId);
        // 2. 商品与明细匹配
        if (!orderItem.getProductId().equals(dto.getProductId())) {
            throw new BusinessException(CommentErrorCode.PRODUCT_MISMATCH);
        }
        // 3. 仅已收货订单可评价
        Order order = orderMapper.selectById(orderItem.getOrderId());
        if (order == null || order.getStatus() == null || order.getStatus() != ORDER_STATUS_RECEIVED) {
            throw new BusinessException(CommentErrorCode.ORDER_NOT_RECEIVED);
        }
        // 4. 幂等：一个订单明细仅一条首次评价
        if (commentMapper.existsByOrderItemId(orderItem.getId()) > 0) {
            throw new BusinessException(CommentErrorCode.ALREADY_COMMENTED);
        }
        Comment comment = Comment.builder()
                .productId(dto.getProductId())
                .userId(userId)
                .orderItemId(orderItem.getId())
                .content(dto.getContent())
                .star(dto.getStar())
                .isAppend(0)
                .build();
        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long append(Long userId, CommentAppendDTO dto) {
        // 1. 仅评论者本人可追评
        Comment comment = requireOwnedComment(dto.getCommentId(), userId);
        // 2. 已删除的评论不可追评
        if (comment.getStatus() != null && comment.getStatus() != 0) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }
        // 3. 仅一级评论可追评
        if (comment.getParentId() != null || Integer.valueOf(1).equals(comment.getIsAppend())) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FIRST_LEVEL);
        }
        // 4. 幂等：一条评论仅一次追评
        if (commentMapper.countChildren(comment.getId(), 1) > 0) {
            throw new BusinessException(CommentErrorCode.ALREADY_APPENDED);
        }
        Comment append = Comment.builder()
                .productId(comment.getProductId())
                .userId(userId)
                .orderItemId(comment.getOrderItemId())
                .parentId(comment.getId())
                .content(dto.getContent())
                .isAppend(1)
                .build();
        commentMapper.insert(append);
        return append.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reply(Long userId, CommentReplyDTO dto) {
        // 1. 仅商家可回复
        if (!UserRoleEnum.MERCHANT.getCode().equals(UserContext.getRole())) {
            throw new BusinessException(CommentErrorCode.ONLY_MERCHANT_REPLY);
        }
        // 2. 被回复的必须是存在的一级评论
        Comment parent = commentMapper.selectById(dto.getParentId());
        if (parent == null || parent.getStatus() != null && parent.getStatus() != 0) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }
        if (parent.getParentId() != null || Integer.valueOf(1).equals(parent.getIsAppend())) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FIRST_LEVEL);
        }
        // 3. 商家资质有效且商品归属校验
        Merchant merchant = merchantMapper.selectByUserId(userId);
        if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
            throw new BusinessException(CommentErrorCode.MERCHANT_INVALID);
        }
        Product product = productMapper.selectById(parent.getProductId());
        if (product == null || !merchant.getId().equals(product.getMerchantId())) {
            throw new BusinessException(CommentErrorCode.PRODUCT_NOT_OWNED);
        }
        Comment reply = Comment.builder()
                .productId(parent.getProductId())
                .userId(userId)
                .orderItemId(parent.getOrderItemId())
                .parentId(parent.getId())
                .content(dto.getContent())
                .isAppend(0)
                .build();
        commentMapper.insert(reply);
        return reply.getId();
    }

    // ================ 私有校验与组装 ================

    private OrderItem requireOwnedOrderItem(Long orderItemId, Long userId) {
        OrderItem orderItem = orderItemMapper.selectById(orderItemId);
        if (orderItem == null) {
            throw new BusinessException(CommentErrorCode.ORDER_ITEM_NOT_FOUND);
        }
        Order order = orderMapper.selectById(orderItem.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(CommentErrorCode.NO_PERMISSION_OPERATE);
        }
        return orderItem;
    }

    private Comment requireOwnedComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(CommentErrorCode.NO_PERMISSION_OPERATE);
        }
        return comment;
    }

    private CommentVO toVO(CommentDetail comment, List<CommentDetail> children) {
        CommentVO.CommentVOBuilder builder = CommentVO.builder()
                .id(comment.getId())
                .productId(comment.getProductId())
                .userId(comment.getUserId())
                .username(comment.getUsername())
                .nickname(comment.getNickname())
                .avatar(comment.getAvatar())
                .orderItemId(comment.getOrderItemId())
                .content(comment.getContent())
                .star(comment.getStar())
                .createdAt(comment.getCreatedAt());
        if (children != null && !children.isEmpty()) {
            builder.append(children.stream().filter(c -> Integer.valueOf(1).equals(c.getIsAppend()))
                            .findFirst().map(this::toChildVO).orElse(null))
                    .replies(children.stream().filter(c -> !Integer.valueOf(1).equals(c.getIsAppend()))
                            .map(this::toChildVO).toList());
        }
        return builder.build();
    }

    private CommentVO toChildVO(CommentDetail child) {
        return CommentVO.builder()
                .id(child.getId())
                .productId(child.getProductId())
                .userId(child.getUserId())
                .username(child.getUsername())
                .nickname(child.getNickname())
                .avatar(child.getAvatar())
                .orderItemId(child.getOrderItemId())
                .content(child.getContent())
                .star(child.getStar())
                .createdAt(child.getCreatedAt())
                .build();
    }
}
