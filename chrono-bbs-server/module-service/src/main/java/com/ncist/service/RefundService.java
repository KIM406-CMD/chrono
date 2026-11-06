package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.RefundCreateDTO;
import com.ncist.dto.RefundReviewDTO;
import com.ncist.vo.RefundVO;

public interface RefundService {

    /** 申请退款/退货 */
    Long apply(Long userId, RefundCreateDTO dto);

    /** 我的退款分页 */
    PageResult<RefundVO> myRefunds(Long userId, Integer page, Integer size);

    /** 退款详情 */
    RefundVO detail(Long userId, Long id);

    /** 审核退款（商家或管理员） */
    void review(Long userId, Long id, RefundReviewDTO dto);
}
