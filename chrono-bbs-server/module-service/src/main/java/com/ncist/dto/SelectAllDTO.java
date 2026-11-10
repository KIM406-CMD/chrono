package com.ncist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 全选/取消全选入参。
 *
 * <p>商城端将当前用户购物车中的所有商品项批量设为选中或未选中。</p>
 */
@Data
public class SelectAllDTO {

    /** 目标选中状态，必填：1=全选，0=全不选。 */
    @NotNull(message = "selected 不能为空")
    private Integer selected;
}
