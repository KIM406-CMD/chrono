package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "收货地址", description = "收货地址的增删改查、设置默认地址等接口")
@RestController
@RequestMapping("/api/address")
public class AddressController {
}