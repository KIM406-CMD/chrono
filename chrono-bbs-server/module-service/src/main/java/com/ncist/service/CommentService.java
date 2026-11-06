package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.CommentAppendDTO;
import com.ncist.dto.CommentCreateDTO;
import com.ncist.dto.CommentReplyDTO;
import com.ncist.vo.CommentVO;

public interface CommentService {

    /** 商品评论列表（公开，含追评与商家回复） */
    PageResult<CommentVO> listByProduct(Long productId, Integer star, Integer page, Integer size);

    /** 发表评论（仅已收货订单明细） */
    Long create(Long userId, CommentCreateDTO dto);

    /** 追加评论（仅一级评论，评论者本人） */
    Long append(Long userId, CommentAppendDTO dto);

    /** 商家回复（仅自己商品的评论） */
    Long reply(Long userId, CommentReplyDTO dto);
}
