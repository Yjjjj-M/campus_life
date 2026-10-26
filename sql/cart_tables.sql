-- 订单商品明细表
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(255) COMMENT '商品图片',
    price INT NOT NULL COMMENT '单价（分）',
    quantity INT NOT NULL COMMENT '数量',
    subtotal INT NOT NULL COMMENT '小计（分）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) COMMENT='订单商品明细表';

-- 修改订单表，支持商品订单
ALTER TABLE orders ADD COLUMN total_price INT DEFAULT 0 COMMENT '商品总价（分）';
ALTER TABLE orders ADD COLUMN coupon_id BIGINT COMMENT '使用的优惠券ID';
ALTER TABLE orders ADD COLUMN discount INT DEFAULT 0 COMMENT '优惠金额（分）';
ALTER TABLE orders MODIFY COLUMN voucher_id BIGINT NULL COMMENT '优惠券ID（可为空）';
