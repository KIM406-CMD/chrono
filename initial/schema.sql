-- =====================================================
-- 网上超市系统 - 建表脚本
-- 数据库: chrono_bbs
-- =====================================================

CREATE DATABASE IF NOT EXISTS chrono_bbs
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 创建专用数据库用户（Docker 环境用 '%' 允许外部连接）
CREATE USER IF NOT EXISTS 'chrono'@'%' IDENTIFIED BY 'chrono123';
GRANT ALL PRIVILEGES ON chrono_bbs.* TO 'chrono'@'%';
FLUSH PRIVILEGES;

USE chrono_bbs;

-- =====================================================
-- 1. 用户表
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `phone`      VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`      VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar`     VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `role`       TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
    `status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-禁用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_email` (`email`),
    INDEX `idx_role` (`role`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- =====================================================
-- 2. 商家表
-- =====================================================
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant`
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`          BIGINT       NOT NULL COMMENT '关联user表',
    `shop_name`        VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `business_license` VARCHAR(500) DEFAULT NULL COMMENT '营业执照URL',
    `contact_phone`    VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
    `review_reason`    VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `reviewed_at`      DATETIME     DEFAULT NULL COMMENT '审核时间',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商家表';

-- =====================================================
-- 3. 分类表（三级）
-- =====================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `parent_id`  BIGINT       NOT NULL DEFAULT 0 COMMENT '父级分类ID，0表示一级',
    `level`      TINYINT      NOT NULL COMMENT '层级：1/2/3',
    `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `icon`       VARCHAR(500) DEFAULT NULL COMMENT '分类图标URL',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_level` (`level`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='分类表';

-- =====================================================
-- 4. 规格模板表
-- =====================================================
DROP TABLE IF EXISTS `spec_template`;
CREATE TABLE `spec_template`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       VARCHAR(50) NOT NULL COMMENT '规格名（如颜色、尺码）',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='规格模板表';

-- =====================================================
-- 5. 规格值表
-- =====================================================
DROP TABLE IF EXISTS `spec_value`;
CREATE TABLE `spec_value`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id` BIGINT      NOT NULL COMMENT '关联spec_template',
    `value`       VARCHAR(50) NOT NULL COMMENT '规格值（如红色、XL）',
    `sort_order`  INT         NOT NULL DEFAULT 0 COMMENT '排序序号',
    PRIMARY KEY (`id`),
    INDEX `idx_template_id` (`template_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='规格值表';

-- =====================================================
-- 6. 商品表
-- =====================================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`
(
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(200)  NOT NULL COMMENT '商品名称',
    `description` TEXT          DEFAULT NULL COMMENT '商品描述（富文本）',
    `images`      VARCHAR(2000) DEFAULT NULL COMMENT '图片URL数组（JSON，最多4张）',
    `brand`       VARCHAR(100)  DEFAULT NULL COMMENT '品牌',
    `category_id` BIGINT        NOT NULL COMMENT '关联三级分类',
    `merchant_id` BIGINT        NOT NULL COMMENT '关联merchant表',
    `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0-下架，1-上架',
    `sales`       INT           NOT NULL DEFAULT 0 COMMENT '累计销量',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建/上架时间',
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_merchant_id` (`merchant_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_category_status` (`category_id`, `status`),
    INDEX `idx_status_sales` (`status`, `sales`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_brand` (`brand`),
    FULLTEXT INDEX `ft_product` (`name`, `description`, `brand`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品表';

-- =====================================================
-- 7. SKU表
-- =====================================================
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` BIGINT       NOT NULL COMMENT '关联product表',
    `spec_ids`   VARCHAR(200) DEFAULT NULL COMMENT '规格值ID组合（JSON数组）',
    `spec_text`  VARCHAR(200) DEFAULT NULL COMMENT '规格文本（如红色/XL）',
    `price`      INT          NOT NULL COMMENT '价格（单位：分）',
    `stock`      INT          NOT NULL DEFAULT 0 COMMENT '库存数量',
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='SKU表';

-- =====================================================
-- 8. 购物车表
-- =====================================================
DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item`
(
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT   NOT NULL COMMENT '关联user表',
    `product_id` BIGINT   NOT NULL COMMENT '关联product表',
    `sku_id`     BIGINT   NOT NULL COMMENT '关联product_sku表',
    `quantity`   INT      NOT NULL DEFAULT 1 COMMENT '数量',
    `selected`   TINYINT  NOT NULL DEFAULT 1 COMMENT '0-未选中，1-已选中',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_sku` (`user_id`, `sku_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='购物车表';

-- =====================================================
-- 9. 订单表
-- =====================================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`         VARCHAR(32)  NOT NULL COMMENT '订单编号，唯一',
    `user_id`          BIGINT       NOT NULL COMMENT '关联user表',
    `address_snapshot` VARCHAR(500) DEFAULT NULL COMMENT '收货地址快照（JSON）',
    `total_amount`     INT          NOT NULL COMMENT '实付金额（单位：分）',
    `status`           TINYINT      NOT NULL COMMENT '订单状态码',
    `cancel_reason`    VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
    `paid_at`          DATETIME     DEFAULT NULL COMMENT '支付时间',
    `shipped_at`       DATETIME     DEFAULT NULL COMMENT '发货时间',
    `received_at`      DATETIME     DEFAULT NULL COMMENT '收货时间',
    `completed_at`     DATETIME     DEFAULT NULL COMMENT '完成时间',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_user_status` (`user_id`, `status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';

-- =====================================================
-- 10. 订单明细表
-- =====================================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`
(
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`   BIGINT NOT NULL COMMENT '关联order表',
    `product_id` BIGINT NOT NULL COMMENT '关联product表',
    `sku_id`     BIGINT NOT NULL COMMENT '关联product_sku表',
    `quantity`   INT    NOT NULL COMMENT '购买数量',
    `unit_price` INT    NOT NULL COMMENT '下单时单价（分）',
    `subtotal`   INT    NOT NULL COMMENT '小计（分）',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_sku_id` (`sku_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单明细表';

-- =====================================================
-- 11. 支付记录表
-- =====================================================
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`   BIGINT      NOT NULL COMMENT '关联order表',
    `pay_method` TINYINT     NOT NULL COMMENT '支付方式：1-卡号支付，2-二维码支付',
    `card_no`    VARCHAR(20) DEFAULT NULL COMMENT '卡号',
    `status`     TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0-处理中，1-成功，2-失败',
    `paid_at`    DATETIME    DEFAULT NULL COMMENT '支付成功时间',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='支付记录表';

-- =====================================================
-- 12. 退款/退货表
-- =====================================================
DROP TABLE IF EXISTS `refund`;
CREATE TABLE `refund`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`      BIGINT       NOT NULL COMMENT '关联order表',
    `order_item_id` BIGINT       NOT NULL COMMENT '关联order_item（支持部分退款）',
    `user_id`       BIGINT       NOT NULL COMMENT '申请人',
    `type`          TINYINT      NOT NULL COMMENT '类型：1-退款，2-退货',
    `reason`        VARCHAR(500) NOT NULL COMMENT '申请原因',
    `amount`        INT          NOT NULL COMMENT '退款金额（分）',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
    `reviewer_id`   BIGINT       DEFAULT NULL COMMENT '审核人',
    `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `reviewed_at`   DATETIME     DEFAULT NULL COMMENT '审核时间',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_order_item_id` (`order_item_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='退款/退货表';

-- =====================================================
-- 13. 收货地址表
-- =====================================================
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT       NOT NULL COMMENT '关联user表',
    `receiver_name` VARCHAR(50)  NOT NULL COMMENT '收货人姓名',
    `phone`         VARCHAR(20)  NOT NULL COMMENT '联系电话',
    `province`      VARCHAR(50)  NOT NULL COMMENT '省',
    `city`          VARCHAR(50)  NOT NULL COMMENT '市',
    `district`      VARCHAR(50)  NOT NULL COMMENT '区',
    `detail`        VARCHAR(200) NOT NULL COMMENT '详细地址',
    `is_default`    TINYINT      NOT NULL DEFAULT 0 COMMENT '0-否，1-默认地址',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='收货地址表';

-- =====================================================
-- 14. 评论表（多级嵌套）
-- =====================================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`
(
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id`    BIGINT        NOT NULL COMMENT '关联product表',
    `user_id`       BIGINT        NOT NULL COMMENT '关联user表',
    `order_item_id` BIGINT        DEFAULT NULL COMMENT '关联order_item',
    `parent_id`     BIGINT        DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论',
    `content`       VARCHAR(1000) NOT NULL COMMENT '评论内容',
    `star`          TINYINT       DEFAULT NULL COMMENT '星级评分（1-5），仅一级评论有值',
    `is_append`     TINYINT       NOT NULL DEFAULT 0 COMMENT '0-首次评价，1-追评',
    `status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-已删除',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_product_status` (`product_id`, `status`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_order_item_id` (`order_item_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='评论表';

-- =====================================================
-- 15. 搜索历史表
-- =====================================================
DROP TABLE IF EXISTS `search_history`;
CREATE TABLE `search_history`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '关联user表',
    `keyword`     VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    `searched_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_searched_at` (`searched_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='搜索历史表';

-- =====================================================
-- 16. 会话表
-- =====================================================
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`           BIGINT       NOT NULL COMMENT '普通用户ID',
    `merchant_id`       BIGINT       NOT NULL COMMENT '商家用户ID',
    `product_id`        BIGINT       DEFAULT NULL COMMENT '关联的商品ID',
    `last_message`      VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息摘要',
    `last_message_time` DATETIME     DEFAULT NULL COMMENT '最后一条消息时间',
    `user_unread`       INT          NOT NULL DEFAULT 0 COMMENT '用户未读数',
    `merchant_unread`   INT          NOT NULL DEFAULT 0 COMMENT '商家未读数',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_merchant` (`user_id`, `merchant_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_merchant_id` (`merchant_id`),
    INDEX `idx_updated_at` (`updated_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='会话表';

-- =====================================================
-- 17. 消息表
-- =====================================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`
(
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `conversation_id` BIGINT        NOT NULL COMMENT '关联conversation表',
    `sender_id`       BIGINT        NOT NULL COMMENT '发送者ID',
    `sender_role`     TINYINT       NOT NULL COMMENT '发送者角色：0-用户，1-商家',
    `content`         VARCHAR(2000) NOT NULL COMMENT '消息内容',
    `type`            TINYINT       NOT NULL DEFAULT 1 COMMENT '消息类型：1-文本，2-图片',
    `is_read`         TINYINT       NOT NULL DEFAULT 0 COMMENT '0-未读，1-已读',
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_conversation_read` (`conversation_id`, `is_read`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息表';

-- =====================================================
-- 18. 秒杀券表
-- =====================================================
DROP TABLE IF EXISTS `seckill_coupon`;
CREATE TABLE `seckill_coupon`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`           VARCHAR(100) NOT NULL COMMENT '券名称',
    `description`    VARCHAR(500) DEFAULT NULL COMMENT '券描述',
    `discount_type`  TINYINT      NOT NULL COMMENT '优惠类型：1-满减券，2-折扣券',
    `discount_value` INT          NOT NULL COMMENT '优惠值：满减券为减多少分，折扣券为打几折（如85表示8.5折）',
    `min_amount`     INT          NOT NULL DEFAULT 0 COMMENT '最低消费金额（分），0表示无门槛',
    `total_count`    INT          NOT NULL COMMENT '发放总量',
    `remain_count`   INT          NOT NULL COMMENT '剩余数量',
    `start_time`     DATETIME     NOT NULL COMMENT '开始时间',
    `end_time`       DATETIME     NOT NULL COMMENT '结束时间',
    `per_user_limit` INT          NOT NULL DEFAULT 1 COMMENT '每人限领数量',
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_end_time` (`end_time`),
    INDEX `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='秒杀券表';

-- =====================================================
-- 19. 用户领券表
-- =====================================================
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon`
(
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT   NOT NULL COMMENT '关联user表',
    `coupon_id`     BIGINT   NOT NULL COMMENT '关联seckill_coupon表',
    `status`        TINYINT  NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
    `used_order_id` BIGINT   DEFAULT NULL COMMENT '使用的订单ID',
    `claim_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `used_time`     DATETIME DEFAULT NULL COMMENT '使用时间',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_coupon_id` (`coupon_id`),
    INDEX `idx_user_coupon` (`user_id`, `coupon_id`),
    INDEX `idx_user_status` (`user_id`, `status`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户领券表';

-- =====================================================
-- 20. 操作日志表
-- =====================================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `username`   VARCHAR(50)  DEFAULT NULL COMMENT '操作人用户名',
    `ip`         VARCHAR(50)  DEFAULT NULL COMMENT '操作IP',
    `module`     VARCHAR(50)  DEFAULT NULL COMMENT '模块',
    `action`     VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    `params`     TEXT         DEFAULT NULL COMMENT '请求参数（JSON）',
    `result`     VARCHAR(50)  DEFAULT NULL COMMENT '结果（成功/失败）',
    `cost_time`  BIGINT       DEFAULT NULL COMMENT '耗时（ms）',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_module` (`module`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志表';