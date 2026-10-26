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
public class Payment {

    private Long id;
    private Long orderId;
    private Integer payMethod;
    private String cardNo;
    private Integer status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}