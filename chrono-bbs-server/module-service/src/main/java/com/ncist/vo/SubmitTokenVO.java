package com.ncist.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 防重复提交 Token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTokenVO {

    private String token;
}
