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
 * 操作日志实体，对应 {@code operation_log} 表。
 *
 * <p>由 AOP 切面在标注了操作日志注解的 Service 方法执行后自动写入，
 * 用于管理后台审计与问题排查。</p>
 */
@Builder
public class OperationLog {

    private Long id;
    /** 操作人用户 ID；系统任务或未登录操作可为空。 */
    private Long userId;
    /** 操作人用户名快照，避免用户改名后日志失真。 */
    private String username;
    /** 操作来源 IP。 */
    private String ip;
    /** 所属业务模块，如“管理员”“订单”。 */
    private String module;
    /** 操作描述。 */
    private String action;
    /** 请求参数（JSON 字符串），可能包含敏感信息，展示时需脱敏。 */
    private String params;
    /** 执行结果，如“成功”“失败”。 */
    private String result;
    /** 方法执行耗时，单位：毫秒。 */
    private Long costTime;
    /** 操作时间。 */
    private LocalDateTime createdAt;
}