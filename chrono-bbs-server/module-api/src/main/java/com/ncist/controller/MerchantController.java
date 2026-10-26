package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商家管理", description = "商家入驻申请、审核、店铺管理等接口")
@RestController
@RequestMapping("/api/merchant")
public class MerchantController {
}