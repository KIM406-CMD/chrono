package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台", description = "管理员专用的用户管理、商家审核、数据报表等接口")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
}