package com.ncist.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total;
    private Integer page;
    private Integer size;
    private List<T> list;

    public static <T> PageResult<T> of(Long total, Integer page, Integer size, List<T> list) {
        return new PageResult<>(total, page, size, list);
    }
}