package com.ncist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建/修改商品请求。
 *
 * <p>商家维护自己店铺的商品时使用；创建时 skuList 至少包含一个 SKU，
 * 修改时 skuList 非空则全量替换原有 SKU。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {

    /** 商品名称，创建时必填。 */
    @NotBlank(message = "商品名称不能为空")
    private String name;
    /** 商品描述，可选。 */
    private String description;
    /** 商品图片 URL 列表，可选；服务端序列化为 JSON 存储。 */
    private List<String> images;
    /** 品牌名称，可选。 */
    private String brand;
    /** 三级分类 ID，创建时必填，且分类必须存在。 */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    /** SKU 列表，创建时至少一个；修改时非空则全量替换原有 SKU。 */
    private List<ProductSkuDTO> skuList;
}
