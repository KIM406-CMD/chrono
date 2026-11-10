package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 防重复提交 Token。
 *
 * <p>创建订单前获取，下单请求必须携带且仅可使用一次。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTokenVO {

    /** 一次性提交 Token。 */
    private String token;
}
