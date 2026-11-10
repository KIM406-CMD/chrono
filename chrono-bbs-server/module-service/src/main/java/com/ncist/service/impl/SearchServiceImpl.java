package com.ncist.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ncist.common.enums.SearchErrorCode;
import com.ncist.common.exception.BusinessException;
import com.ncist.common.result.PageResult;
import com.ncist.document.ProductDoc;
import com.ncist.mapper.SearchHistoryMapper;
import com.ncist.service.ProductSearchIndexService;
import com.ncist.service.SearchService;
import com.ncist.vo.SearchItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 商品搜索服务实现：检索/建议走 Elasticsearch，搜索历史与热搜走 MySQL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final String INDEX = ProductSearchIndexService.INDEX;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int SUGGEST_SIZE = 10;
    private static final int HISTORY_LIMIT = 20;
    private static final int HOT_LIMIT = 10;
    private static final int HOT_DAYS = 7;
    /** 仅搜索上架商品 */
    private static final int STATUS_ON = 1;
    /** 关键词最大长度（对应 search_history.keyword VARCHAR(100)） */
    private static final int KEYWORD_MAX_LENGTH = 100;
    /** ES 默认深分页上限 */
    private static final int MAX_RESULT_WINDOW = 10000;

    private final ElasticsearchClient elasticsearchClient;
    private final SearchHistoryMapper searchHistoryMapper;

    @Override
    public PageResult<SearchItemVO> search(String keyword, Long categoryId, String brand, Integer minPrice,
                                           Integer maxPrice, String sort, Integer page, Integer size, Long userId) {
        // 分页参数兜底：page 至少 1，size 默认 20、上限 100
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        String kw = keyword == null ? null : keyword.trim();
        // keyword 与 categoryId 都为空时无检索条件，直接返回空分页
        if ((kw == null || kw.isEmpty()) && categoryId == null) {
            return PageResult.of(0L, p, s, Collections.emptyList());
        }
        // ES 默认 max_result_window=10000，超出范围的深分页直接返回空，避免请求报错
        if ((long) (p - 1) * s >= MAX_RESULT_WINDOW) {
            return PageResult.of(0L, p, s, Collections.emptyList());
        }
        // 关键词命中时记录搜索历史（未登录不记录）
        if (kw != null && !kw.isEmpty() && userId != null) {
            saveHistory(userId, kw);
        }
        try {
            SearchResponse<ProductDoc> response = elasticsearchClient.search(
                    buildSearchRequest(kw, categoryId, brand, minPrice, maxPrice, sort, (p - 1) * s, s),
                    ProductDoc.class);
            long total = response.hits().total() == null ? response.hits().hits().size()
                    : response.hits().total().value();
            List<SearchItemVO> list = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(this::toItemVO)
                    .toList();
            return PageResult.of(total, p, s, list);
        } catch (Exception e) {
            log.error("商品搜索失败：{}", e.getMessage());
            throw new BusinessException(SearchErrorCode.SEARCH_UNAVAILABLE);
        }
    }

    @Override
    public List<String> suggest(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            SearchResponse<ProductDoc> response = elasticsearchClient.search(req -> req
                    .index(INDEX)
                    .size(SUGGEST_SIZE)
                    .source(src -> src.filter(f -> f.includes("name")))
                    .query(q -> q.bool(b -> b
                            .filter(f -> f.term(t -> t.field("status").value(STATUS_ON)))
                            .must(m -> m.matchBoolPrefix(mb -> mb.field("name").query(kw))))),
                    ProductDoc.class);
            // 同名商品去重后按命中顺序返回
            Set<String> names = new LinkedHashSet<>();
            for (Hit<ProductDoc> hit : response.hits().hits()) {
                if (hit.source() != null && hit.source().getName() != null) {
                    names.add(hit.source().getName());
                }
            }
            return new ArrayList<>(names);
        } catch (Exception e) {
            // 自动补全失败不影响主流程，返回空列表
            log.warn("搜索建议失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> history(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return searchHistoryMapper.selectRecent(userId, HISTORY_LIMIT);
    }

    @Override
    public void clearHistory(Long userId) {
        if (userId == null) {
            return;
        }
        searchHistoryMapper.deleteByUserId(userId);
    }

    @Override
    public List<String> hotKeywords() {
        return searchHistoryMapper.selectHot(HOT_DAYS, HOT_LIMIT);
    }

    // ================ 私有方法 ================

    /** 写搜索历史：超长截断，同关键词先删后插保证去重且最新 */
    private void saveHistory(Long userId, String keyword) {
        String kw = keyword.length() > KEYWORD_MAX_LENGTH
                ? keyword.substring(0, KEYWORD_MAX_LENGTH) : keyword;
        try {
            searchHistoryMapper.deleteByUserAndKeyword(userId, kw);
            searchHistoryMapper.insert(userId, kw);
        } catch (Exception e) {
            // 历史记录失败不影响搜索主流程
            log.warn("保存搜索历史失败：{}", e.getMessage());
        }
    }

    /** 构建检索请求：name/description/brand 多字段匹配 + 分类/品牌/价格过滤 + 上架过滤 + 排序 */
    private SearchRequest buildSearchRequest(String keyword, Long categoryId, String brand, Integer minPrice,
                                             Integer maxPrice, String sort, int from, int size) {
        return SearchRequest.of(req -> {
            req.index(INDEX).from(from).size(size)
                    .trackTotalHits(t -> t.enabled(true))
                    .sort(buildSortOptions(keyword, sort));
            return req.query(q -> q.bool(b -> {
                // 只搜上架商品
                b.filter(f -> f.term(t -> t.field("status").value(STATUS_ON)));
                if (categoryId != null) {
                    b.filter(f -> f.term(t -> t.field("categoryId").value(categoryId)));
                }
                if (brand != null && !brand.isBlank()) {
                    b.filter(f -> f.term(t -> t.field("brand").value(brand.trim())));
                }
                if (minPrice != null || maxPrice != null) {
                    b.filter(f -> f.range(r -> r.number(n -> {
                        n.field("minPrice");
                        if (minPrice != null) {
                            n.gte(minPrice.doubleValue());
                        }
                        if (maxPrice != null) {
                            n.lte(maxPrice.doubleValue());
                        }
                        return n;
                    })));
                }
                if (keyword != null && !keyword.isEmpty()) {
                    // operator=AND：分词后所有词项都要命中，避免"可乐"命中"乐事"这类部分匹配
                    b.must(m -> m.multiMatch(mm -> mm
                            .query(keyword)
                            .operator(Operator.And)
                            .fields("name^3", "description", "brand")));
                }
                return b;
            }));
        });
    }

    /** 排序：默认综合（有关键词按相关性，然后销量），也可显式指定销量/价格/时间 */
    private List<SortOptions> buildSortOptions(String keyword, String sort) {
        String s = sort == null ? "" : sort;
        return switch (s) {
            case "sales_desc" -> List.of(fieldSort("sales", SortOrder.Desc));
            case "price_asc" -> List.of(fieldSort("minPrice", SortOrder.Asc));
            case "price_desc" -> List.of(fieldSort("minPrice", SortOrder.Desc));
            case "time_desc" -> List.of(fieldSort("createdAt", SortOrder.Desc));
            default -> {
                List<SortOptions> options = new ArrayList<>();
                if (keyword != null && !keyword.isEmpty()) {
                    options.add(SortOptions.of(o -> o.score(sc -> sc.order(SortOrder.Desc))));
                }
                options.add(fieldSort("sales", SortOrder.Desc));
                yield options;
            }
        };
    }

    private SortOptions fieldSort(String field, SortOrder order) {
        return SortOptions.of(o -> o.field(f -> f.field(field).order(order)));
    }

    /** 索引文档 → 返回 VO（字段严格对应 api-doc 15.1） */
    private SearchItemVO toItemVO(ProductDoc doc) {
        return SearchItemVO.builder()
                .id(doc.getId())
                .name(doc.getName())
                .description(doc.getDescription())
                .brand(doc.getBrand())
                .categoryId(doc.getCategoryId())
                .categoryName(doc.getCategoryName())
                .minPrice(doc.getMinPrice())
                .sales(doc.getSales())
                .image(doc.getImage())
                .build();
    }
}
