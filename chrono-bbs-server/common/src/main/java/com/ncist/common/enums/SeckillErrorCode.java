package com.ncist.common.enums;

import lombok.Getter;

/**
 * 秒杀券模块错误码
 */
@Getter
public enum SeckillErrorCode implements ErrorCode {

    SECKILL_NOT_FOUND(404, "秒杀券不存在"),
    SECKILL_SOLD_OUT(409, "秒杀券已被抢光"),
    SECKILL_ALREADY_CLAIMED(409, "已领取过该秒杀券"),
    SECKILL_NOT_ACTIVE(400, "秒杀券活动未开始或已结束"),
    BELOW_MIN_AMOUNT(400, "不满足最低消费门槛"),
    COUPON_NOT_FOUND(404, "优惠券不存在"),
    COUPON_USED_OR_EXPIRED(409, "优惠券已过期或已使用"),
    NO_PERMISSION_MANAGE(403, "仅商家或管理员可管理秒杀券"),
    SECKILL_DELETE_ONLY_NOT_STARTED(400, "仅未开始的秒杀券可删除"),
    TIME_FORMAT_ILLEGAL(400, "时间格式必须为 yyyy-MM-dd HH:mm:ss"),
    TIME_RANGE_ILLEGAL(400, "结束时间必须晚于开始时间"),
    DISCOUNT_TYPE_ILLEGAL(400, "优惠类型仅允许 1-满减券 或 2-折扣券"),
    DISCOUNT_VALUE_ILLEGAL(400, "优惠值非法：满减券须大于 0 分，折扣券须为 1-99 的折数（如 85 表示 8.5 折）"),
    STATUS_ILLEGAL(400, "状态参数非法");

    private final Integer code;
    private final String message;

    SeckillErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
