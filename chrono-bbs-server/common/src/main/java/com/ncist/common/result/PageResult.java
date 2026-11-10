package com.ncist.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果。
 *
 * <p>统一封装总记录数、当前页、每页大小与当前页数据，供前端分页组件使用。</p>
 *
 * @param <T> 列表元素类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 符合条件的总记录数。 */
    private Long total;
    /** 当前页码，从 1 开始。 */
    private Integer page;
    /** 每页记录数。 */
    private Integer size;
    /** 当前页数据列表。 */
    private List<T> list;

    /**
     * 创建分页结果。
     *
     * @param total 总记录数
     * @param page  当前页码，从 1 开始
     * @param size  每页记录数
     * @param list  当前页数据
     * @param <T>   列表元素类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long total, Integer page, Integer size, List<T> list) {
        return new PageResult<>(total, page, size, list);
    }
}