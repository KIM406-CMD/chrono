package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.MerchantApplyDTO;
import com.ncist.vo.MerchantInfoVO;
import com.ncist.vo.MerchantOrderVO;
import com.ncist.vo.MerchantStatsVO;

import java.util.Map;

public interface MerchantService {

    /** 入驻申请（待审核） */
    void apply(Long userId, MerchantApplyDTO dto);

    /** 获取当前商家信息 */
    MerchantInfoVO getInfo(Long userId);

    /** 商家商品分页列表 */
    PageResult<Map<String, Object>> listProducts(Long userId, Integer status, Integer page, Integer size);

    /** 商家订单分页列表 */
    PageResult<MerchantOrderVO> listOrders(Long userId, Integer status, Integer page, Integer size);

    /** 商家发货 */
    void ship(Long userId, Long orderId);

    /** 销售统计 */
    MerchantStatsVO getStats(Long userId, String startDate, String endDate);
}
