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
public class Product {

    private Long id;
    private String name;
    private String description;
    private String images;
    private String brand;
    private Long categoryId;
    private Long merchantId;
    private Integer status;
    private Integer sales;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}