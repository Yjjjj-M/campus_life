-- 修改订单表，让 shop_id 允许为空
ALTER TABLE orders MODIFY COLUMN shop_id BIGINT NULL COMMENT '商铺ID（购物车订单可为空）';
