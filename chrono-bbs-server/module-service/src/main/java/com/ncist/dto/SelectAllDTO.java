package com.ncist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 全选/取消全选入参
 */
@Data
public class SelectAllDTO {

    @NotNull(message = "selected 不能为空")
    private Integer selected;
}
