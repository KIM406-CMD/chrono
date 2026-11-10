package com.ncist.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncist.document.ProductDoc;
import com.ncist.entity.Category;
import com.ncist.entity.Product;
import com.ncist.entity.ProductMinPrice;
import com.ncist.mapper.CategoryMapper;
import com.ncist.mapper.ProductMapper;
import com.ncist.mapper.ProductSkuMapper;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品搜索索引服务：索引创建、全量初始化、增量同步
 * 索引数据仅作为搜索用途，同步失败只记录日志，不影响商品主业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchIndexService {

    /** 搜索索引名 */
    public static final String INDEX = "chrono_product";

    /** 索引 mapping：name/description 分词检索，brand/categoryId/status 过滤，minPrice/sales 排序 */
    private static final String MAPPING_JSON = """
            {
              "settings": {
                "number_of_shards": 1,
                "number_of_replicas": 0
              },
              "mappings": {
                "properties": {
                  "id":           { "type": "long" },
                  "name":         { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
                  "description":  { "type": "text" },
                  "brand":        { "type": "keyword" },
                  "categoryId":   { "type": "long" },
                  "categoryName": { "type": "keyword" },
                  "minPrice":     { "type": "integer" },
                  "sales":        { "type": "integer" },
                  "image":        { "type": "keyword", "index": false },
                  "status":       { "type": "integer" },
                  "createdAt":    { "type": "date", "format": "yyyy-MM-dd HH:mm:ss||strict_date_optional_time" }
                }
              }
            }
            """;

    /** 全量同步分页大小 */
    private static final int SYNC_PAGE_SIZE = 500;
    /** 上架状态 */
    private static final int STATUS_ON = 1;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ElasticsearchClient elasticsearchClient;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;

    /** 应用启动后：确保索引存在，索引为空时执行全量初始化（ES 不可用时仅告警） */
    @EventListener(ApplicationReadyEvent.class)
    public void initOnStartup() {
        try {
            ensureIndex();
            long docCount = elasticsearchClient.count(c -> c.index(INDEX)).count();
            if (docCount == 0 && productMapper.countByCategory(null) > 0) {
                int synced = syncAll();
                log.info("搜索索引全量初始化完成，共同步 {} 条商品", synced);
            }
        } catch (Exception e) {
            log.warn("搜索索引初始化失败（不影响应用启动）：{}", e.getMessage());
        }
    }

    /** 索引不存在时创建（含 mapping） */
    public void ensureIndex() throws Exception {
        boolean exists = elasticsearchClient.indices().exists(e -> e.index(INDEX)).value();
        if (exists) {
            return;
        }
        elasticsearchClient.indices().create(c -> c.index(INDEX).withJson(new StringReader(MAPPING_JSON)));
        log.info("搜索索引 {} 创建成功", INDEX);
    }

    /** 全量同步上架商品（按 id 覆盖写，可重复执行） */
    public int syncAll() throws Exception {
        Map<Long, String> categoryNames = categoryNameMap();
        int offset = 0;
        int total = 0;
        while (true) {
            List<Product> products = productMapper.selectListPage(null, offset, SYNC_PAGE_SIZE);
            if (products.isEmpty()) {
                break;
            }
            List<ProductDoc> docs = toDocs(products, categoryNames);
            if (!docs.isEmpty()) {
                bulkIndex(docs);
                total += docs.size();
            }
            offset += SYNC_PAGE_SIZE;
            if (products.size() < SYNC_PAGE_SIZE) {
                break;
            }
        }
        return total;
    }

    /** 增量同步单个商品：上架则写入/覆盖，下架或已删除则移除文档；异常只记日志，不影响商品主业务 */
    public void syncProduct(Long productId) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() == null || product.getStatus() != STATUS_ON) {
                deleteProduct(productId);
                return;
            }
            ensureIndex();
            ProductDoc doc = toDocs(List.of(product), categoryNameMap()).get(0);
            indexDocument(productId, doc);
        } catch (Exception e) {
            log.warn("商品 {} 同步搜索索引失败：{}", productId, e.getMessage());
        }
    }

    /** 删除商品对应的索引文档（文档本不存在时忽略）；异常只记日志，不影响商品主业务 */
    public void deleteProduct(Long productId) {
        try {
            elasticsearchClient.delete(d -> d.index(INDEX).id(String.valueOf(productId)));
        } catch (ElasticsearchException e) {
            // 404 表示索引中本来就没有该文档（如下架商品重复编辑），无需告警
            if (e.status() != 404) {
                log.warn("商品 {} 删除搜索索引失败：{}", productId, e.getMessage());
            }
        } catch (Exception e) {
            log.warn("商品 {} 删除搜索索引失败：{}", productId, e.getMessage());
        }
    }

    private void indexDocument(Long productId, ProductDoc doc) throws Exception {
        elasticsearchClient.index(i -> i.index(INDEX).id(String.valueOf(productId)).document(doc));
    }

    // ================ 私有组装方法 ================

    /** 组装索引文档：图片解析、最低价、分类名 */
    private List<ProductDoc> toDocs(List<Product> products, Map<Long, String> categoryNames) {
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        Map<Long, Integer> minPriceMap = productSkuMapper.selectMinPrices(productIds).stream()
                .collect(Collectors.toMap(ProductMinPrice::getProductId, ProductMinPrice::getMinPrice));
        return products.stream()
                .map(product -> ProductDoc.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .brand(product.getBrand())
                        .categoryId(product.getCategoryId())
                        .categoryName(categoryNames.get(product.getCategoryId()))
                        .minPrice(minPriceMap.get(product.getId()))
                        .sales(product.getSales())
                        .image(firstImage(product.getImages()))
                        .status(product.getStatus())
                        .createdAt(product.getCreatedAt() == null ? null
                                : product.getCreatedAt().format(DATE_TIME_FORMATTER))
                        .build())
                .collect(Collectors.toList());
    }

    /** 批量写入索引 */
    private void bulkIndex(List<ProductDoc> docs) throws Exception {
        BulkRequest.Builder builder = new BulkRequest.Builder();
        for (ProductDoc doc : docs) {
            builder.operations(op -> op.index(idx -> idx
                    .index(INDEX)
                    .id(String.valueOf(doc.getId()))
                    .document(doc)));
        }
        BulkResponse response = elasticsearchClient.bulk(builder.build());
        if (response.errors()) {
            log.warn("搜索索引批量写入存在失败项：{}", response.items().stream()
                    .filter(item -> item.error() != null)
                    .findFirst()
                    .map(item -> item.error().reason())
                    .orElse("未知原因"));
        }
    }

    /** 分类 id → 名称 */
    private Map<Long, String> categoryNameMap() {
        Map<Long, String> map = new LinkedHashMap<>();
        for (Category category : categoryMapper.selectAll()) {
            map.put(category.getId(), category.getName());
        }
        return map;
    }

    /** 取 images JSON 的第一张图 */
    private String firstImage(String images) {
        if (images == null || images.isBlank()) {
            return null;
        }
        try {
            List<String> list = objectMapper.readValue(images, new TypeReference<List<String>>() {
            });
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}
