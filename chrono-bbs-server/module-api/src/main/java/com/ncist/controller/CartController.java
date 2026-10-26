package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "购物车", description = "购物车的增删改查、选中/取消等接口")
@RestController
@RequestMapping("/api/cart")
public class CartController {
}