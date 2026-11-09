package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规格值实体，对应 {@code spec_value} 表。
 *
 * <p>隶属于一个 {@link SpecTemplate}，是生成 SKU 规格组合的最小选项，
 * 如“颜色”模板下的“红色”。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecValue {

    private Long id;
    /** 所属规格模板 ID，关联 spec_template 表。 */
    private Long templateId;
    /** 规格值，如“红色”“XL”。 */
    private String value;
    /** 同级排序序号，值越小越靠前。 */
    private Integer sortOrder;
}