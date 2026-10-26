# Campus Life Platform Database Schema
# Version: 1.0
# Date: 2026-01-20

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table: user (用户表)
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `password` varchar(128) NOT NULL COMMENT 'BCrypt加密密码',
  `nickname` varchar(32) DEFAULT '' COMMENT '昵称',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色：0=普通用户, 1=管理员',
  `icon` varchar(255) DEFAULT '' COMMENT '头像URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- Table: shop_category (商铺分类)
-- ----------------------------
DROP TABLE IF EXISTS `shop_category`;
CREATE TABLE `shop_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(32) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT '' COMMENT '图标URL',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=启用, 0=禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商铺分类表';

-- ----------------------------
-- Table: shop (商铺)
-- ----------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(128) NOT NULL COMMENT '商铺名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `images` varchar(2048) NOT NULL COMMENT '图片URL，逗号分隔',
  `area` varchar(64) DEFAULT '' COMMENT '商圈/区域',
  `address` varchar(255) NOT NULL COMMENT '详细地址',
  `avg_price` int DEFAULT 0 COMMENT '人均价格（分）',
  `score` int DEFAULT 0 COMMENT '评分（1-50，显示时除以10）',
  `sold` int DEFAULT 0 COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=营业, 0=休息',
  `open_hours` varchar(64) DEFAULT '' COMMENT '营业时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商铺表';

-- ----------------------------
-- Table: voucher (优惠券/活动)
-- ----------------------------
DROP TABLE IF EXISTS `voucher`;
CREATE TABLE `voucher` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint NOT NULL COMMENT '商铺ID',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `sub_title` varchar(255) DEFAULT '' COMMENT '副标题',
  `rules` varchar(1024) DEFAULT '' COMMENT '使用规则',
  `pay_value` int NOT NULL COMMENT '支付金额（分）',
  `actual_value` int NOT NULL COMMENT '抵扣金额（分）',
  `type` tinyint NOT NULL COMMENT '类型：0=普通券, 1=秒杀券',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=上架, 2=下架, 3=过期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券/活动表';

-- ----------------------------
-- Table: seckill_voucher (秒杀库存)
-- ----------------------------
DROP TABLE IF EXISTS `seckill_voucher`;
CREATE TABLE `seckill_voucher` (
  `voucher_id` bigint NOT NULL COMMENT '关联的券ID',
  `stock` int NOT NULL COMMENT '库存数量',
  `begin_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀库存表';

-- ----------------------------
-- Table: orders (订单)
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint NOT NULL COMMENT '订单ID（RedisIdWorker生成）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `voucher_id` bigint NOT NULL COMMENT '关联的券ID',
  `shop_id` bigint NOT NULL COMMENT '商铺ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=未支付, 2=已支付, 3=已核销, 4=已取消, 5=已退款',
  `pay_type` tinyint NOT NULL DEFAULT 1 COMMENT '支付方式：1=余额(模拟), 2=支付宝, 3=微信',
  `amount` int NOT NULL COMMENT '实付金额（分）',
  `create_time` datetime NOT NULL COMMENT '下单时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `use_time` datetime DEFAULT NULL COMMENT '核销时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_voucher` (`user_id`, `voucher_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_voucher_id` (`voucher_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ----------------------------
-- Table: order_detail (订单明细)
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `item_name` varchar(128) NOT NULL COMMENT '商品名称',
  `item_image` varchar(255) DEFAULT '' COMMENT '商品图片',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `amount` int NOT NULL COMMENT '金额（分）',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ----------------------------
-- Initial admin user (password: admin123, BCrypt encrypted)
-- ----------------------------
INSERT INTO `user` (`phone`, `password`, `nickname`, `role`, `icon`) 
VALUES ('13800000000', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理员', 1, '');
-- Password: admin123 (BCrypt encrypted)

SET FOREIGN_KEY_CHECKS = 1;
