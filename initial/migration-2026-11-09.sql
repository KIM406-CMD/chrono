-- =====================================================
-- 2026-11-09 增量迁移：退货寄回确认 + 评论图片
--
-- 可重复执行：列/索引已存在时自动跳过，不会报错。
-- 适用：已存在的 chrono_bbs_dev 库；Docker 首次初始化也会在
--       schema.sql / data.sql 之后自动执行本脚本（此时为空操作）。
-- =====================================================

USE chrono_bbs_dev;

-- ---------------- comment.images ----------------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'comment' AND COLUMN_NAME = 'images'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `comment` ADD COLUMN `images` VARCHAR(2000) DEFAULT NULL COMMENT ''图片URL数组（JSON，最多4张）'' AFTER `content`',
    'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------- refund.merchant_id ----------------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund' AND COLUMN_NAME = 'merchant_id'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `refund` ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT ''关联merchant表主键'' AFTER `user_id`',
    'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------- refund.return_company ----------------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund' AND COLUMN_NAME = 'return_company'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `refund` ADD COLUMN `return_company` VARCHAR(50) DEFAULT NULL COMMENT ''退货快递公司'' AFTER `reviewed_at`',
    'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------- refund.return_logistics_no ----------------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund' AND COLUMN_NAME = 'return_logistics_no'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `refund` ADD COLUMN `return_logistics_no` VARCHAR(50) DEFAULT NULL COMMENT ''退货物流单号'' AFTER `return_company`',
    'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------- refund.returned_at ----------------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund' AND COLUMN_NAME = 'returned_at'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `refund` ADD COLUMN `returned_at` DATETIME DEFAULT NULL COMMENT ''买家寄回时间'' AFTER `return_logistics_no`',
    'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------- refund.return_received_at ----------------
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund' AND COLUMN_NAME = 'return_received_at'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `refund` ADD COLUMN `return_received_at` DATETIME DEFAULT NULL COMMENT ''商家确认收货时间'' AFTER `returned_at`',
    'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------- refund.idx_merchant_id ----------------
SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund' AND INDEX_NAME = 'idx_merchant_id'
);
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE `refund` ADD INDEX `idx_merchant_id` (`merchant_id`)',
    'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------- refund.status 注释（幂等） ----------------
ALTER TABLE `refund`
    MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 0
    COMMENT '状态：0-待审核，1-已通过，2-已拒绝，3-待寄回，4-待商家收货，5-退货完成';
