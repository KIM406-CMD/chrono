package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSku {

    private Long id;
    private Long productId;
    private String specIds;
    private String specText;
    private Integer price;
    private Integer stock;
    private Integer status;
    private LocalDateTime createdAt;
}