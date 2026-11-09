-- =====================================================
-- 2026-11-09 增量迁移：退货寄回确认 + 评论图片
--
-- 适用：已存在的 chrono_bbs_dev 库（保留原有数据）。
-- 全新初始化请直接使用 schema.sql，无需执行本文件。
-- =====================================================

USE chrono_bbs_dev;

-- 评论图片（最多 4 张，JSON 数组）
ALTER TABLE `comment`
    ADD COLUMN `images` VARCHAR(2000) DEFAULT NULL COMMENT '图片URL数组（JSON，最多4张）' AFTER `content`;

-- 退款/退货：商家归属、退货物流与收货时间
ALTER TABLE `refund`
    ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT '关联merchant表主键' AFTER `user_id`,
    ADD COLUMN `return_company` VARCHAR(50) DEFAULT NULL COMMENT '退货快递公司' AFTER `reviewed_at`,
    ADD COLUMN `return_logistics_no` VARCHAR(50) DEFAULT NULL COMMENT '退货物流单号' AFTER `return_company`,
    ADD COLUMN `returned_at` DATETIME DEFAULT NULL COMMENT '买家寄回时间' AFTER `return_logistics_no`,
    ADD COLUMN `return_received_at` DATETIME DEFAULT NULL COMMENT '商家确认收货时间' AFTER `returned_at`,
    ADD INDEX `idx_merchant_id` (`merchant_id`);

-- 更新状态注释（0-5）
ALTER TABLE `refund`
    MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 0
    COMMENT '状态：0-待审核，1-已通过，2-已拒绝，3-待寄回，4-待商家收货，5-退货完成';
