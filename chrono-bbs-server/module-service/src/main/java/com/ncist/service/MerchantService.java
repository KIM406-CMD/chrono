package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.MerchantApplyDTO;
import com.ncist.vo.MerchantInfoVO;
import com.ncist.vo.MerchantOrderVO;
import com.ncist.vo.MerchantStatsVO;

import java.util.Map;

/**
 * 商家管理服务。
 *
 * <p>负责商家入驻申请、商家信息查询、商品与订单管理、发货以及销售统计。
 * 除入驻申请外，其余方法要求当前用户是已审核通过（status=1）的商家，
 * 且只能操作归属自己的商品与订单。</p>
 *
 * <p>入驻申请与发货由实现类以事务保证一致性；分页参数不合法时
 * 回退为第 1 页、每页 10 条。</p>
 */
public interface MerchantService {

    /**
     * 提交商家入驻申请，初始状态为待审核。
     *
     * @param userId 申请用户 ID，必须为当前登录用户
     * @param dto    申请参数，包含店铺名称、营业执照与联系电话
     * @throws BusinessException 该用户已存在商家记录时抛出
     */
    void apply(Long userId, MerchantApplyDTO dto);

    /**
     * 查询当前用户的商家信息（不要求审核通过）。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @return 商家信息，包含审核状态与审核意见
     * @throws BusinessException 用户不存在商家记录时抛出
     */
    MerchantInfoVO getInfo(Long userId);

    /**
     * 分页查询当前商家的商品。
     *
     * @param userId 商家用户 ID，必须为已审核通过的商家
     * @param status 商品状态过滤：0-下架，1-上架，为空时不过滤
     * @param page   页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size   每页条数；为空或小于 1 时按 10 处理
     * @return 商品分页结果，每项以 Map 形式返回商品字段
     * @throws BusinessException 用户非商家或商家资质未通过时抛出
     */
    PageResult<Map<String, Object>> listProducts(Long userId, Integer status, Integer page, Integer size);

    /**
     * 分页查询当前商家的订单（仅包含属于该商家的订单项）。
     *
     * @param userId 商家用户 ID，必须为已审核通过的商家
     * @param status 订单状态过滤，为空时不过滤
     * @param page   页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size   每页条数；为空或小于 1 时按 10 处理
     * @return 商家订单分页结果
     * @throws BusinessException 用户非商家或商家资质未通过时抛出
     */
    PageResult<MerchantOrderVO> listOrders(Long userId, Integer status, Integer page, Integer size);

    /**
     * 商家发货，将已支付订单更新为已发货。
     *
     * @param userId  商家用户 ID，必须为已审核通过的商家
     * @param orderId 订单 ID，订单内商品必须全部属于该商家
     * @throws BusinessException 用户非商家/资质未通过、订单不存在、订单不属于该商家
     *                           或订单当前状态不允许发货时抛出
     */
    void ship(Long userId, Long orderId);

    /**
     * 查询当前商家的销售统计。
     *
     * @param userId    商家用户 ID，必须为已审核通过的商家
     * @param startDate 统计开始日期，格式 yyyy-MM-dd，可为空表示不限
     * @param endDate   统计结束日期，格式 yyyy-MM-dd，可为空表示不限
     * @return 销售统计，包含销售额、订单数、热销商品、分类销售与趋势
     * @throws BusinessException 用户非商家或商家资质未通过时抛出
     */
    MerchantStatsVO getStats(Long userId, String startDate, String endDate);
}
