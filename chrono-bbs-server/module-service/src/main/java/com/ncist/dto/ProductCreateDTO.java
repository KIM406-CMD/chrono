package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建/修改商品请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {

    @NotBlank(message = "商品名称不能为空")
    private String name;
    private String description;
    private List<String> images;
    private String brand;
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    private List<ProductSkuDTO> skuList;
}
