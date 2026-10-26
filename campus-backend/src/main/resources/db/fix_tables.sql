-- 修复数据库表结构（补全缺失的表和字段）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 1. 修复 orders 表（添加缺失字段）
-- ========================================
ALTER TABLE orders 
ADD COLUMN order_no VARCHAR(64) DEFAULT NULL COMMENT '订单编号' AFTER id,
ADD COLUMN pay_value INT DEFAULT 0 COMMENT '实付金额（分）' AFTER status,
ADD COLUMN actual_value INT DEFAULT 0 COMMENT '抵扣金额（分）' AFTER pay_value,
ADD COLUMN total_price INT DEFAULT 0 COMMENT '商品总价（分）' AFTER actual_value,
ADD COLUMN coupon_id BIGINT DEFAULT NULL COMMENT '使用的优惠券ID' AFTER total_price,
ADD COLUMN discount INT DEFAULT 0 COMMENT '优惠金额（分）' AFTER coupon_id;

-- 添加索引
ALTER TABLE orders ADD INDEX idx_order_no (order_no);

-- ========================================
-- 2. 创建 product_category 表（商品分类）
-- ========================================
CREATE TABLE IF NOT EXISTS `product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=启用, 0=禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 插入默认商品分类
INSERT INTO product_category (name, sort, status) VALUES
('热销商品', 1, 1),
('新品上市', 2, 1),
('限时特惠', 3, 1),
('日用百货', 4, 1),
('食品饮料', 5, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ========================================
-- 3. 创建 product 表（商品）
-- ========================================
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` bigint NOT NULL COMMENT '商铺ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `name` varchar(128) NOT NULL COMMENT '商品名称',
  `price` int NOT NULL DEFAULT 0 COMMENT '价格（分）',
  `image` varchar(255) DEFAULT '' COMMENT '图片URL',
  `description` text COMMENT '商品描述',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=上架, 0=下架',
  `sales` int NOT NULL DEFAULT 0 COMMENT '销量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ========================================
-- 4. 创建 order_item 表（订单明细项）
-- ========================================
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(128) NOT NULL COMMENT '商品名称',
  `product_image` varchar(255) DEFAULT '' COMMENT '商品图片',
  `price` int NOT NULL DEFAULT 0 COMMENT '单价（分）',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `subtotal` int NOT NULL DEFAULT 0 COMMENT '小计（分）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细项表';

-- ========================================
-- 5. 创建 review_like 表（评价点赞）
-- ========================================
CREATE TABLE IF NOT EXISTS `review_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `review_id` bigint NOT NULL COMMENT '评价ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_review` (`user_id`, `review_id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价点赞表';

-- ========================================
-- 6. 创建 follow 表（关注）
-- ========================================
CREATE TABLE IF NOT EXISTS `follow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `follow_user_id` bigint NOT NULL COMMENT '被关注用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
  KEY `idx_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';

-- ========================================
-- 7. 检查 review 表是否存在
-- ========================================
CREATE TABLE IF NOT EXISTS `review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `shop_id` bigint NOT NULL COMMENT '商铺ID',
  `rating` tinyint NOT NULL DEFAULT 5 COMMENT '评分（1-5）',
  `content` text COMMENT '评价内容',
  `images` varchar(2048) DEFAULT '' COMMENT '图片URL，逗号分隔',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=正常, 0=隐藏',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商铺评价表';

SET FOREIGN_KEY_CHECKS = 1;
