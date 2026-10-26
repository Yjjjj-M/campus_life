-- 一次性修复订单表所有 NOT NULL 字段冲突
-- 让购物车订单和优惠券订单都能正常插入

-- shop_id: 购物车订单可能包含多个商铺
ALTER TABLE orders MODIFY COLUMN shop_id BIGINT NULL COMMENT '商铺ID（购物车订单可为空）';

-- voucher_id: 购物车订单不使用优惠券时为空
ALTER TABLE orders MODIFY COLUMN voucher_id BIGINT NULL COMMENT '优惠券ID（可为空）';

-- actual_value: 购物车订单无抵扣金额概念，设为默认0
ALTER TABLE orders MODIFY COLUMN actual_value INT DEFAULT 0 COMMENT '抵扣金额（分）';
