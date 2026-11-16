-- =====================================================
-- 2026-11-16 增量迁移：站内通知表
--
-- 可重复执行：表已存在时自动跳过，不会报错。
-- 适用：已存在的 chrono_bbs_dev 库；Docker 首次初始化也会在
--       schema.sql / data.sql 之后自动执行本脚本（此时为空操作）。
-- =====================================================

USE chrono_bbs_dev;

-- ---------------- notification ----------------
CREATE TABLE IF NOT EXISTS `notification`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT       NOT NULL COMMENT '接收用户ID',
    `type`         TINYINT      NOT NULL DEFAULT 1 COMMENT '通知类型：1-订单通知，2-系统通知，3-售后通知',
    `title`        VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content`      VARCHAR(500) NOT NULL COMMENT '通知正文',
    `related_type` TINYINT      DEFAULT NULL COMMENT '关联对象类型：1-订单，2-退款',
    `related_id`   BIGINT       DEFAULT NULL COMMENT '关联对象ID，如订单ID',
    `is_read`      TINYINT      NOT NULL DEFAULT 0 COMMENT '阅读状态：0-未读，1-已读',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_read` (`user_id`, `is_read`),
    INDEX `idx_user_created` (`user_id`, `created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='站内通知表';
