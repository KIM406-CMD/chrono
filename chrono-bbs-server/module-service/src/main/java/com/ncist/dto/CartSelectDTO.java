package com.ncist.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

/**
 * 选中/取消选中购物车项入参
 * <p>
 * 兼容两种传参：
 * 1. api-doc：cartItemId（单个）+ selected（1/0）
 * 2. 前端：cartItemIds（批量）+ selected（布尔）；均不传时视为全选/全不选
 */
@Data
public class CartSelectDTO {

    private Long cartItemId;

    private List<Long> cartItemIds;

    /** 原始 JSON 值（布尔或整型均可） */
    private JsonNode selected;

    /** 归一化为 1/0 */
    public Integer selectedFlag() {
        if (selected == null) {
            return null;
        }
        if (selected.isBoolean()) {
            return selected.asBoolean() ? 1 : 0;
        }
        return selected.asInt();
    }
}
