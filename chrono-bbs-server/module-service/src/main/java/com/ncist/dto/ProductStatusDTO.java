package com.ncist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品上下架请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusDTO {

    private Integer status;
}
