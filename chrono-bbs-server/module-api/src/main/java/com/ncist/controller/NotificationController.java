package com.ncist.controller;

import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.service.NotificationService;
import com.ncist.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 站内通知控制器。
 *
 * <p>为登录用户提供通知列表、未读数统计与已读操作；
 * 通知由下单、支付、发货等业务节点自动产生。接口前缀 {@code /api/notification}。</p>
 */
@Tag(name = "站内通知", description = "订单通知、系统通知、未读数与已读操作")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 分页查询当前用户的站内通知。
     *
     * @param unreadOnly 是否仅查询未读，默认查询全部
     * @param page       页码，从 1 开始，默认 1
     * @param size       每页条数，默认 10
     * @return 通知分页结果
     */
    @Operation(summary = "我的通知列表（分页）")
    @GetMapping("/list")
    public Result<PageResult<NotificationVO>> list(
            @RequestParam(required = false) @Parameter(description = "仅看未读") Boolean unreadOnly,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(notificationService.list(UserContext.getUserId(), unreadOnly, page, size));
    }

    /**
     * 查询当前用户的未读通知数量。
     *
     * @return 未读数，字段 {@code count}
     */
    @Operation(summary = "未读通知数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.success(Map.of("count", notificationService.unreadCount(UserContext.getUserId())));
    }

    /**
     * 将指定通知标记为已读。
     *
     * @param id 通知 ID
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "标记单条通知为已读")
    @Parameter(name = "id", description = "通知 ID", example = "1")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(UserContext.getUserId(), id);
        return Result.success();
    }

    /**
     * 将当前用户全部未读通知标记为已读。
     *
     * @return 本次标记为已读的数量
     */
    @Operation(summary = "全部标记为已读")
    @PutMapping("/read-all")
    public Result<Integer> markAllRead() {
        return Result.success(notificationService.markAllRead(UserContext.getUserId()));
    }
}
