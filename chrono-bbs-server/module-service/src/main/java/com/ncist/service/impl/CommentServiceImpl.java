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
 * 评论服务实现。
 *
 * <p>负责商品评价的查询、发表、追评与商家回复。列表查询先分页获取一级评论，
 * 再批量查询其子评论并按父评论分组，避免 N+1 查询；
 * 所有写操作使用 {@code @Transactional} 保证单据与评论记录的一致性。</p>
 *
 * <p>业务约束：仅已收货订单的明细可评价，且每个明细只能评价一次；
 * 追评仅限原评论者且每条一级评论仅一次；商家回复需通过资质与商品归属校验。</p>
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    /** 订单状态：已收货/已完成，仅该状态允许评价。 */
    private static final int ORDER_STATUS_RECEIVED = 4;

    /** 评论数据访问组件。 */
    private final CommentMapper commentMapper;
    /** 订单数据访问组件，用于校验订单状态与属主。 */
    private final OrderMapper orderMapper;
    /** 订单明细数据访问组件，用于校验评价来源。 */
    private final OrderItemMapper orderItemMapper;
    /** 商品数据访问组件，用于商家回复时校验商品归属。 */
    private final ProductMapper productMapper;
    /** 商家数据访问组件，用于校验商家资质。 */
    private final MerchantMapper merchantMapper;

    /** {@inheritDoc} */
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

    /**
     * {@inheritDoc}
     *
     * <p>依次校验订单明细属主、商品匹配、订单已收货与评价幂等，全部通过后写入评论。</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>追评记录以原评论 ID 作为 parent_id，并以 is_append=1 标识。</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>回复记录以被回复的一级评论 ID 作为 parent_id，并以 is_append=0 标识。</p>
     */
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

    /**
     * 查询订单明细并校验其所属订单属于当前用户。
     *
     * @param orderItemId 订单明细 ID
     * @param userId      当前用户 ID
     * @return 订单明细实体
     * @throws BusinessException 明细不存在或所属订单不属于当前用户时抛出
     */
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

    /**
     * 查询评论并校验作者为当前用户。
     *
     * @param commentId 评论 ID
     * @param userId    当前用户 ID
     * @return 评论实体
     * @throws BusinessException 评论不存在或不属于当前用户时抛出
     */
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

    /**
     * 将一级评论及其子评论组装为视图对象。
     *
     * <p>子评论中 is_append=1 的第一条作为追评，其余作为商家回复列表。</p>
     *
     * @param comment  一级评论明细
     * @param children 该评论的子评论（追评与回复）
     * @return 评论视图对象
     */
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

    /**
     * 将子评论（追评或回复）转换为视图对象。
     *
     * @param child 子评论明细
     * @return 子评论视图对象
     */
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
