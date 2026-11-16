-- =====================================================
-- 2026-11-16 增量迁移：按类目拆分演示商品归属
--
-- 背景：批量演示商品早期全部归属 merchant1，导致卖家中心
--       列表混入零食、服饰、家居等不同类目商品。
-- 处理：新增“家居服饰馆”商家（merchant3），并按类目重新归属：
--       - 服饰/家居（7、401、402、403）        → merchant3
--       - 数码/家电/个护（303、5001~5003、502、503）→ merchant2
--       - 食品/饮料/果蔬等其余类目             → merchant1
--
-- 可重复执行：账号/店铺使用 INSERT IGNORE，商品归属按类目重算。
-- 适用：已执行过 initial/batch-products.sql 的 chrono_bbs_dev 库。
-- =====================================================

SET NAMES utf8mb4;
USE chrono_bbs_dev;

-- ---------------- 家居服饰演示商家 ----------------
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `nickname`, `phone`, `email`, `role`, `status`) VALUES
(101, 'merchant3', '$2a$10$GwJpq50PdP9bSlAKy8Jg5.qvNMdUEehQjAoJ2ciQ3U8rgei6Sx50u', '家居服饰', '13900000003', 'merchant3@chrono.local', 2, 1);

INSERT IGNORE INTO `merchant` (`id`, `user_id`, `shop_name`, `business_license`, `contact_phone`, `status`, `review_reason`, `reviewed_at`) VALUES
(3, 101, '家居服饰馆', 'https://picsum.photos/seed/license-home/800/600', '13900000003', 1, '资质齐全，审核通过', '2026-10-20 10:20:00');

-- ---------------- 批量商品按类目重新归属 ----------------
UPDATE `product`
SET `merchant_id` = CASE
    WHEN `category_id` IN (7, 401, 402, 403) THEN 3
    WHEN `category_id` IN (303, 5001, 5002, 5003, 502, 503) THEN 2
    ELSE 1
END
WHERE `id` BETWEEN 30001 AND 30186;

-- ---------------- 演示商品归属兜底 ----------------
UPDATE `product` SET `merchant_id` = 1 WHERE `id` BETWEEN 10001 AND 10006;
UPDATE `product` SET `merchant_id` = 2 WHERE `id` BETWEEN 20001 AND 20006;
