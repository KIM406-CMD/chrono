package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 搜索历史实体，对应 {@code search_history} 表。
 *
 * <p>记录用户搜索关键词，用于“我的搜索历史”展示与热词统计；
 * 旧记录可按时间定期清理。</p>
 */
@Builder
public class SearchHistory {

    private Long id;
    /** 搜索用户 ID，关联 user 表。 */
    private Long userId;
    /** 搜索关键词，最长 100 字符。 */
    private String keyword;
    /** 搜索时间。 */
    private LocalDateTime searchedAt;
}