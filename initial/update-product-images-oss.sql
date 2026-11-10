-- =====================================================
-- 同步商品图片路径至阿里云 OSS（针对已初始化的数据库）
--
-- 用途：demo-data.sql 使用 INSERT IGNORE，对已存在的商品记录不会更新
--       images 字段。若 Docker/本地 MySQL 已初始化过，需手动执行本脚本
--       将 12 条演示商品的图片路径从前端静态资源替换为 OSS 源图 URL。
-- 使用：mysql -h <host> -P <port> -u chrono -pchrono123 chrono_bbs_dev < update-product-images-oss.sql
-- =====================================================

SET NAMES utf8mb4;
USE chrono_bbs_dev;

UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/食品饮料/水果/1794229751483.png"]' WHERE `id` = 10001;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/食品饮料/水果/1794229746515.png"]' WHERE `id` = 10002;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/食品饮料/水果/1794229307477.png"]' WHERE `id` = 10003;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/食品饮料/水果/1794229751483.png"]' WHERE `id` = 10004;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/食品饮料/水果/1794229304109.png"]' WHERE `id` = 10005;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/食品饮料/水果/1794229310307.png"]' WHERE `id` = 10006;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/电子产品/手机/1794229094917.png"]' WHERE `id` = 20001;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/电子产品/手机/1794229089817.png"]' WHERE `id` = 20002;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/电子产品/耳机/1794229170945.png"]' WHERE `id` = 20003;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/电子产品/手表/1794229444281.png"]' WHERE `id` = 20004;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/电子产品/充电宝/1794229610154.png"]' WHERE `id` = 20005;
UPDATE `product` SET `images` = '["https://chrono-bbs.oss-cn-beijing.aliyuncs.com/img/电子产品/耳机/1794229394246.png"]' WHERE `id` = 20006;

-- 验证
SELECT `id`, `name`, `images` FROM `product`
WHERE `id` IN (10001,10002,10003,10004,10005,10006,20001,20002,20003,20004,20005,20006)
ORDER BY `id`;