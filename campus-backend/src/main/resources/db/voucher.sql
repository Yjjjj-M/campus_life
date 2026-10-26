-- 优惠券表
CREATE TABLE IF NOT EXISTS voucher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    shop_id BIGINT NOT NULL COMMENT '商铺ID',
    title VARCHAR(255) NOT NULL COMMENT '优惠券标题',
    sub_title VARCHAR(500) COMMENT '副标题',
    rules TEXT COMMENT '使用规则',
    pay_value INT NOT NULL COMMENT '实付金额（分）',
    actual_value INT NOT NULL COMMENT '抵扣金额（分）',
    type TINYINT NOT NULL DEFAULT 0 COMMENT '类型：0=普通券, 1=秒杀券',
    status TINYINT NOT NULL DEFAULT 2 COMMENT '状态：1=上架, 2=下架, 3=过期',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    begin_time DATETIME COMMENT '开始时间（秒杀券专用）',
    end_time DATETIME COMMENT '结束时间（秒杀券专用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_shop_id (shop_id),
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';
