package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.vo.SearchItemVO;

import java.util.List;

/**
 * 商品搜索服务
 */
public interface SearchService {

    /**
     * 全文检索（ES）。keyword 与 categoryId 至少提供其一，均为空时返回空分页；
     * userId 不为空时额外写入搜索历史
     */
    PageResult<SearchItemVO> search(String keyword, Long categoryId, String brand, Integer minPrice,
                                    Integer maxPrice, String sort, Integer page, Integer size, Long userId);

    /** 搜索建议：按名称前缀匹配，返回商品名数组 */
    List<String> suggest(String keyword);

    /** 搜索历史：最近 20 条（去重） */
    List<String> history(Long userId);

    /** 清空搜索历史 */
    void clearHistory(Long userId);

    /** 热搜榜：近 7 天搜索频率 TOP 10 */
    List<String> hotKeywords();
}
