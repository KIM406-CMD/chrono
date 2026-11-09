package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规格模板实体，对应 {@code spec_template} 表。
 *
 * <p>表示一类规格维度（如“颜色”“尺码”），其下包含若干 {@link SpecValue}；
 * 商家发布商品时选择模板与规格值组合生成 SKU。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecTemplate {

    private Long id;
    /** 规格名，如“颜色”“尺码”。 */
    private String name;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}