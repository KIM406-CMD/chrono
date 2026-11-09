package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.RefundCreateDTO;
import com.ncist.dto.RefundReviewDTO;
import com.ncist.vo.RefundVO;

/**
 * 退款服务。
 *
 * <p>负责退款/退货申请的提交、查询与审核。退款要求订单处于已支付、已发货或已收货状态，
 * 同一订单明细仅允许发起一次退款；审核通过后订单标记为已退款，拒绝后恢复原状态。</p>
 *
 * <p>申请与审核均涉及退款单与订单状态的联动写操作，由实现类以事务保证一致性。</p>
 */
public interface RefundService {

    /**
     * 申请退款/退货。
     *
     * <p>校验订单明细属主、订单状态、退款金额（大于 0 且不超过明细小计），
     * 同一订单明细仅允许申请一次；申请成功后订单进入退款中状态。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    退款参数，包含订单明细 ID、退款类型、原因与金额
     * @return 新增退款单 ID
     * @throws BusinessException 订单明细不存在/不属于当前用户、订单状态不可退款、
     *                           已存在退款申请或退款金额非法时抛出
     */
    Long apply(Long userId, RefundCreateDTO dto);

    /**
     * 分页查询当前用户的退款记录。
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param page   页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size   每页条数；为空或小于 1 时按 10 处理
     * @return 退款分页结果
     */
    PageResult<RefundVO> myRefunds(Long userId, Integer page, Integer size);

    /**
     * 查询退款详情。
     *
     * @param userId 用户 ID，必须为退款申请人
     * @param id     退款单 ID
     * @return 退款详情
     * @throws BusinessException 退款单不存在或不属于当前用户时抛出
     */
    RefundVO detail(Long userId, Long id);

    /**
     * 审核退款申请。
     *
     * <p>管理员可直接审核；商家需资质有效且退款商品归属自己。
     * 状态条件更新保证同一退款单仅能被审核一次。审核通过后订单标记为已退款，
     * 拒绝后恢复为原订单状态。</p>
     *
     * @param userId 审核人用户 ID，必须为管理员或相关商家的用户
     * @param id     退款单 ID
     * @param dto    审核参数，包含是否通过及审核原因
     * @throws BusinessException 退款单不存在、无审核权限或退款单已被审核时抛出
     */
    void review(Long userId, Long id, RefundReviewDTO dto);
}
