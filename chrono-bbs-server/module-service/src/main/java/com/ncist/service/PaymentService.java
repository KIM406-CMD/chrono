package com.ncist.service;

import com.ncist.dto.CardPayDTO;
import com.ncist.vo.PaymentQrVO;
import com.ncist.vo.PayResultVO;

/**
 * 支付服务（模拟支付）。
 *
 * <p>提供银行卡与二维码两种模拟支付方式：银行卡支付会校验卡号 Luhn 校验位，
 * 两种方式均写入支付记录并将订单由待支付更新为已支付。
 * 支付与订单状态更新由实现类以事务保证一致，状态条件更新避免并发重复支付。</p>
 */
public interface PaymentService {

    /**
     * 银行卡支付（模拟）。
     *
     * <p>校验订单归属与待支付状态、卡号 Luhn 合法性，写入脱敏支付记录后
     * 将订单更新为已支付。</p>
     *
     * @param userId 用户 ID，必须为当前登录用户
     * @param dto    支付参数，包含订单 ID 与银行卡号
     * @return 支付结果，包含支付状态与支付时间
     * @throws BusinessException 订单不存在、不属于当前用户、状态不可支付、
     *                           卡号非法或并发下订单已被支付时抛出
     */
    PayResultVO payByCard(Long userId, CardPayDTO dto);

    /**
     * 获取支付二维码（模拟）。
     *
     * @param userId  用户 ID，必须为当前登录用户
     * @param orderId 订单 ID，必须处于待支付状态且属于当前用户
     * @return 二维码信息，包含订单 ID、金额与二维码图片 URL
     * @throws BusinessException 订单不存在、不属于当前用户或状态不可支付时抛出
     */
    PaymentQrVO getQrcode(Long userId, Long orderId);

    /**
     * 确认二维码支付（模拟），将订单由待支付更新为已支付。
     *
     * @param userId  用户 ID，必须为当前登录用户
     * @param orderId 订单 ID，必须处于待支付状态且属于当前用户
     * @return 支付结果，包含支付状态与支付时间
     * @throws BusinessException 订单不存在、不属于当前用户、状态不可支付
     *                           或并发下订单已被支付时抛出
     */
    PayResultVO confirmQrcode(Long userId, Long orderId);
}
