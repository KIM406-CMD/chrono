package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单管理", description = "订单的创建、支付、发货、收货、取消、退款等接口")
@RestController
@RequestMapping("/api/order")
public class OrderController {
}