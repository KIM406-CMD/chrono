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
public class OperationLog {

    private Long id;
    private Long userId;
    private String username;
    private String ip;
    private String module;
    private String action;
    private String params;
    private String result;
    private Long costTime;
    private LocalDateTime createdAt;
}