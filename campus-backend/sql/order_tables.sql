-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `voucher_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `shop_id` BIGINT NOT NULL COMMENT '商铺ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态：1-待支付 2-已支付 3-已使用 4-已取消 5-已退款',
  `pay_value` INT NOT NULL COMMENT '实付金额（分）',
  `actual_value` INT NOT NULL COMMENT '抵扣金额（分）',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `voucher_title` VARCHAR(255) NOT NULL COMMENT '优惠券标题',
  `voucher_sub_title` VARCHAR(255) DEFAULT '' COMMENT '优惠券副标题',
  `shop_name` VARCHAR(128) NOT NULL COMMENT '商铺名称',
  `shop_address` VARCHAR(255) NOT NULL COMMENT '商铺地址',
  `pay_value` INT NOT NULL COMMENT '实付金额（分）',
  `actual_value` INT NOT NULL COMMENT '抵扣金额（分）',
  `rules` TEXT COMMENT '使用规则',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';
