package com.ncist.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    private Long id;
    private Long productId;
    private Long userId;
    private Long orderItemId;
    private Long parentId;
    private String content;
    private Integer star;
    private Integer isAppend;
    private Integer status;
    private LocalDateTime createdAt;
}