package com.ncist.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

/**
 * 选中/取消选中购物车项入参。
 *
 * <p>兼容两种传参：</p>
 * <ol>
 *   <li>api-doc：cartItemId（单个）+ selected（1/0）</li>
 *   <li>前端：cartItemIds（批量）+ selected（布尔）</li>
 * </ol>
 * <p>两者均不传时视为全选/全不选。</p>
 */
@Data
public class CartSelectDTO {

    /** 单个购物车项 ID，可选；与 {@link #cartItemIds} 二选一。 */
    private Long cartItemId;

    /** 批量购物车项 ID，可选；传入时优先按批量处理。 */
    private List<Long> cartItemIds;

    /** 原始 JSON 值（布尔或整型均可）：true/1=选中，false/0=未选中。 */
    private JsonNode selected;

    /**
     * 将原始 JSON 值归一化为 1/0。
     *
     * @return 1=选中，0=未选中；原始值为空时返回 {@code null}
     */
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
