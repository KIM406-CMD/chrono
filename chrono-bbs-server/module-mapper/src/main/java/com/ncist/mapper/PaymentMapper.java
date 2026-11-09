package com.ncist.mapper;

import com.ncist.entity.Payment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录数据访问。
 *
 * <p>模拟支付场景下只做写入，支付流水用于追溯订单的支付方式与结果。</p>
 */
@Mapper
public interface PaymentMapper {

    /**
     * 新增一条支付记录。
     *
     * @param payment 支付信息，orderId/payMethod/status 必填
     * @return 受影响行数
     */
    @Insert("INSERT INTO payment (order_id, pay_method, card_no, status, paid_at) "
            + "VALUES (#{orderId}, #{payMethod}, #{cardNo}, #{status}, #{paidAt})")
    int insert(Payment payment);
}
