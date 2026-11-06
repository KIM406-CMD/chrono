package com.ncist.unit.service;

import com.ncist.common.context.UserContext;
import com.ncist.common.enums.CommentErrorCode;
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
import com.ncist.service.impl.CommentServiceImpl;
import com.ncist.vo.CommentVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CommentServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评论服务单元测试")
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private MerchantMapper merchantMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        UserContext.set(new UserContext.UserInfo(8L, "buyer", 1));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private OrderItem orderItem(long id, long orderId, long productId, int subtotal) {
        return OrderItem.builder()
                .id(id).orderId(orderId).productId(productId)
                .quantity(1).unitPrice(subtotal).subtotal(subtotal)
                .build();
    }

    private Order order(long id, long userId, int status) {
        return Order.builder().id(id).userId(userId).status(status).build();
    }

    private Comment comment(long id, long userId, Long parentId, Integer isAppend) {
        return Comment.builder()
                .id(id).productId(100L).userId(userId)
                .orderItemId(11L).parentId(parentId)
                .content("不错").star(parentId == null && isAppend == 0 ? 5 : null)
                .isAppend(isAppend).status(0)
                .build();
    }

    private CommentDetail detail(long id, long userId, Long parentId, Integer isAppend) {
        CommentDetail d = new CommentDetail();
        d.setId(id);
        d.setProductId(100L);
        d.setUserId(userId);
        d.setOrderItemId(11L);
        d.setParentId(parentId);
        d.setContent(parentId == null && isAppend == 0 ? "不错" : "补充");
        d.setStar(parentId == null && isAppend == 0 ? 5 : null);
        d.setIsAppend(isAppend);
        d.setStatus(0);
        d.setUsername("user" + userId);
        d.setNickname("昵称" + userId);
        return d;
    }

    // ================ 评论列表 ================

    @Test
    @DisplayName("评论列表-组装追评与商家回复")
    void listAssemblesChildren() {
        when(commentMapper.countFirst(100L, null)).thenReturn(1L);
        when(commentMapper.selectFirstPage(100L, null, 0, 10))
                .thenReturn(List.of(detail(1L, 8L, null, 0)));
        CommentDetail append = detail(2L, 8L, 1L, 1);
        CommentDetail reply = detail(3L, 5L, 1L, 0);
        when(commentMapper.selectChildrenByParentIds(List.of(1L))).thenReturn(List.of(append, reply));

        PageResult<CommentVO> result = commentService.listByProduct(100L, null, 1, 10);

        assertEquals(1, result.getTotal());
        CommentVO vo = result.getList().get(0);
        assertEquals("昵称8", vo.getNickname());
        assertNotNull(vo.getAppend());
        assertEquals("补充", vo.getAppend().getContent());
        assertEquals(1, vo.getReplies().size());
        assertEquals("昵称5", vo.getReplies().get(0).getNickname());
    }

    @Test
    @DisplayName("评论列表-无评论返回空页")
    void listEmpty() {
        when(commentMapper.countFirst(100L, 5)).thenReturn(0L);

        PageResult<CommentVO> result = commentService.listByProduct(100L, 5, 1, 10);

        assertEquals(0, result.getTotal());
        assertEquals(0, result.getList().size());
        verify(commentMapper, never()).selectFirstPage(any(), any(), anyInt(), anyInt());
    }

    // ================ 发表评论 ================

    @Test
    @DisplayName("发表评论-已收货订单成功")
    void createSuccess() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 8L, 4));
        when(commentMapper.existsByOrderItemId(11L)).thenReturn(0);

        commentService.create(8L, createDTO(100L, 11L));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getIsAppend());
        assertEquals(5, captor.getValue().getStar());
        assertNull(captor.getValue().getParentId());
    }

    @Test
    @DisplayName("发表评论-订单明细不存在返回404")
    void createItemNotFound() {
        when(orderItemMapper.selectById(11L)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.create(8L, createDTO(100L, 11L)));
        assertEquals(CommentErrorCode.ORDER_ITEM_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    @DisplayName("发表评论-非本人订单返回403")
    void createNotOwner() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 9L, 4));

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.create(8L, createDTO(100L, 11L)));
        assertEquals(CommentErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    @Test
    @DisplayName("发表评论-商品与明细不匹配返回400")
    void createProductMismatch() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 8L, 4));

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.create(8L, createDTO(999L, 11L)));
        assertEquals(CommentErrorCode.PRODUCT_MISMATCH.getCode(), e.getCode());
    }

    @Test
    @DisplayName("发表评论-未收货订单返回400")
    void createNotReceived() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 8L, 2));

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.create(8L, createDTO(100L, 11L)));
        assertEquals(CommentErrorCode.ORDER_NOT_RECEIVED.getCode(), e.getCode());
    }

    @Test
    @DisplayName("发表评论-重复评价返回409")
    void createAlreadyCommented() {
        when(orderItemMapper.selectById(11L)).thenReturn(orderItem(11L, 1L, 100L, 5000));
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 8L, 4));
        when(commentMapper.existsByOrderItemId(11L)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.create(8L, createDTO(100L, 11L)));
        assertEquals(CommentErrorCode.ALREADY_COMMENTED.getCode(), e.getCode());
        verify(commentMapper, never()).insert(any(Comment.class));
    }

    private CommentCreateDTO createDTO(long productId, long orderItemId) {
        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setProductId(productId);
        dto.setOrderItemId(orderItemId);
        dto.setContent("很好用");
        dto.setStar(5);
        return dto;
    }

    // ================ 追评 ================

    @Test
    @DisplayName("追评-本人一级评论成功")
    void appendSuccess() {
        when(commentMapper.selectById(1L)).thenReturn(comment(1L, 8L, null, 0));
        when(commentMapper.countChildren(1L, 1)).thenReturn(0);

        commentService.append(8L, appendDTO(1L));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getIsAppend());
        assertEquals(1L, captor.getValue().getParentId());
    }

    @Test
    @DisplayName("追评-非本人评论返回403")
    void appendNotOwner() {
        when(commentMapper.selectById(1L)).thenReturn(comment(1L, 9L, null, 0));

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.append(8L, appendDTO(1L)));
        assertEquals(CommentErrorCode.NO_PERMISSION_OPERATE.getCode(), e.getCode());
    }

    @Test
    @DisplayName("追评-追评不可再追评返回400")
    void appendNotFirstLevel() {
        when(commentMapper.selectById(2L)).thenReturn(comment(2L, 8L, 1L, 1));

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.append(8L, appendDTO(2L)));
        assertEquals(CommentErrorCode.COMMENT_NOT_FIRST_LEVEL.getCode(), e.getCode());
    }

    @Test
    @DisplayName("追评-重复追评返回409")
    void appendAlreadyAppended() {
        when(commentMapper.selectById(1L)).thenReturn(comment(1L, 8L, null, 0));
        when(commentMapper.countChildren(1L, 1)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.append(8L, appendDTO(1L)));
        assertEquals(CommentErrorCode.ALREADY_APPENDED.getCode(), e.getCode());
        verify(commentMapper, never()).insert(any(Comment.class));
    }

    private CommentAppendDTO appendDTO(long commentId) {
        CommentAppendDTO dto = new CommentAppendDTO();
        dto.setCommentId(commentId);
        dto.setContent("用了三天补充一下");
        return dto;
    }

    // ================ 商家回复 ================

    @Test
    @DisplayName("商家回复-资质有效且商品归属成功")
    void replySuccess() {
        UserContext.set(new UserContext.UserInfo(5L, "seller", 2));
        when(commentMapper.selectById(1L)).thenReturn(comment(1L, 8L, null, 0));
        when(merchantMapper.selectByUserId(5L)).thenReturn(
                Merchant.builder().id(7L).userId(5L).status(1).build());
        when(productMapper.selectById(100L)).thenReturn(
                Product.builder().id(100L).merchantId(7L).build());

        commentService.reply(5L, replyDTO(1L));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getParentId());
        assertEquals(0, captor.getValue().getIsAppend());
    }

    @Test
    @DisplayName("商家回复-非商家返回403")
    void replyNotMerchant() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.reply(8L, replyDTO(1L)));
        assertEquals(CommentErrorCode.ONLY_MERCHANT_REPLY.getCode(), e.getCode());
        verify(commentMapper, never()).insert(any(Comment.class));
    }

    @Test
    @DisplayName("商家回复-商家资质无效返回403")
    void replyMerchantInvalid() {
        UserContext.set(new UserContext.UserInfo(5L, "seller", 2));
        when(commentMapper.selectById(1L)).thenReturn(comment(1L, 8L, null, 0));
        when(merchantMapper.selectByUserId(5L)).thenReturn(
                Merchant.builder().id(7L).userId(5L).status(0).build());

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.reply(5L, replyDTO(1L)));
        assertEquals(CommentErrorCode.MERCHANT_INVALID.getCode(), e.getCode());
    }

    @Test
    @DisplayName("商家回复-回复他人商品评论返回403")
    void replyProductNotOwned() {
        UserContext.set(new UserContext.UserInfo(5L, "seller", 2));
        when(commentMapper.selectById(1L)).thenReturn(comment(1L, 8L, null, 0));
        when(merchantMapper.selectByUserId(5L)).thenReturn(
                Merchant.builder().id(7L).userId(5L).status(1).build());
        when(productMapper.selectById(100L)).thenReturn(
                Product.builder().id(100L).merchantId(88L).build());

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.reply(5L, replyDTO(1L)));
        assertEquals(CommentErrorCode.PRODUCT_NOT_OWNED.getCode(), e.getCode());
    }

    @Test
    @DisplayName("商家回复-追评不可被回复返回400")
    void replyOnAppendRejected() {
        UserContext.set(new UserContext.UserInfo(5L, "seller", 2));
        when(commentMapper.selectById(2L)).thenReturn(comment(2L, 8L, 1L, 1));

        BusinessException e = assertThrows(BusinessException.class,
                () -> commentService.reply(5L, replyDTO(2L)));
        assertEquals(CommentErrorCode.COMMENT_NOT_FIRST_LEVEL.getCode(), e.getCode());
    }

    private CommentReplyDTO replyDTO(long parentId) {
        CommentReplyDTO dto = new CommentReplyDTO();
        dto.setParentId(parentId);
        dto.setContent("感谢您的评价");
        return dto;
    }
}
