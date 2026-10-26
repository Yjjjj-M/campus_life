-- 测试秒杀券数据

-- 1. 插入 3 个秒杀券到 voucher 表
INSERT INTO voucher (id, shop_id, title, sub_title, rules, pay_value, actual_value, type, status, create_time, update_time)
VALUES 
  (1001, 1, '【秒杀】全场8折优惠券', '限时特惠，仅限今日', '每人限购1张', 100, 500, 1, 1, NOW(), NOW()),
  (1002, 2, '【秒杀】满100减50', '超值优惠', '每人限购1张', 200, 1000, 1, 1, NOW(), NOW()),
  (1003, 3, '【秒杀】新人特惠', '新用户专享', '每人限购1张', 50, 300, 1, 1, NOW(), NOW());

-- 2. 插入对应的秒杀库存记录
INSERT INTO seckill_voucher (voucher_id, stock, begin_time, end_time, create_time, update_time)
VALUES 
  (1001, 100, '2026-10-24 00:00:00', '2026-12-31 23:59:59', NOW(), NOW()),
  (1002, 100, '2026-10-24 00:00:00', '2026-12-31 23:59:59', NOW(), NOW()),
  (1003, 100, '2026-10-24 00:00:00', '2026-12-31 23:59:59', NOW(), NOW());

-- 验证
SELECT v.id, v.title, v.type, sv.stock, sv.begin_time, sv.end_time
FROM voucher v
LEFT JOIN seckill_voucher sv ON v.id = sv.voucher_id
WHERE v.type = 1;
