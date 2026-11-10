package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.CommentAppendDTO;
import com.ncist.dto.CommentCreateDTO;
import com.ncist.dto.CommentReplyDTO;
import com.ncist.vo.CommentVO;

/**
 * 商品评论服务。
 *
 * <p>负责商品评价的查询、发表、追评与商家回复。
 * 评论按一级评论分页展示，并一次性聚合其追评与商家回复。</p>
 *
 * <p>发表评论要求订单明细属于当前用户且订单已收货，同一订单明细仅允许评价一次；
 * 追评仅限原评论者本人，且一条一级评论仅允许追评一次；
 * 商家回复要求商家资质有效且商品归属该商家。写操作由实现类以事务保证一致性。</p>
 */
public interface CommentService {

    /**
     * 分页查询商品评论（公开接口，游客可访问）。
     *
     * @param productId 商品 ID
     * @param star      星级过滤（1~5），为空时不过滤
     * @param page      页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size      每页条数；为空或小于 1 时按 10 处理
     * @return 评论分页结果，每条一级评论包含追评与商家回复
     */
    PageResult<CommentVO> listByProduct(Long productId, Integer star, Integer page, Integer size);

    /**
     * 发表商品评价。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    评价参数，包含订单明细 ID、商品 ID、评分与内容
     * @return 新增评论 ID
     * @throws BusinessException 订单明细不存在/不属于当前用户、商品不匹配、
     *                           订单未收货或该明细已评价时抛出
     */
    Long create(Long userId, CommentCreateDTO dto);

    /**
     * 追加评论。
     *
     * @param userId 用户 ID，必须为原评论作者
     * @param dto    追评参数，包含一级评论 ID 与追评内容
     * @return 新增追评 ID
     * @throws BusinessException 评论不存在、不属于当前用户、非一级评论或已追评时抛出
     */
    Long append(Long userId, CommentAppendDTO dto);

    /**
     * 商家回复用户评论。
     *
     * @param userId 商家用户 ID，必须为当前登录用户
     * @param dto    回复参数，包含被回复的一级评论 ID 与回复内容
     * @return 新增回复 ID
     * @throws BusinessException 当前用户非商家、评论不存在/非一级评论、
     *                           商家资质无效或商品不属于该商家时抛出
     */
    Long reply(Long userId, CommentReplyDTO dto);
}
