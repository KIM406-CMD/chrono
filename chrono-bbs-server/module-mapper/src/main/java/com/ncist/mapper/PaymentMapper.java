package com.ncist.mapper;

import com.ncist.entity.Payment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录数据访问
 */
@Mapper
public interface PaymentMapper {

    @Insert("INSERT INTO payment (order_id, pay_method, card_no, status, paid_at) "
            + "VALUES (#{orderId}, #{payMethod}, #{cardNo}, #{status}, #{paidAt})")
    int insert(Payment payment);
}
