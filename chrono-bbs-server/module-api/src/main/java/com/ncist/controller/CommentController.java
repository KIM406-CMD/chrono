package com.ncist.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论管理", description = "商品评论、追评、回复、星级评分等接口")
@RestController
@RequestMapping("/api/comment")
public class CommentController {
}