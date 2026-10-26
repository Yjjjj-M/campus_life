-- 插入测试优惠券数据
SET NAMES utf8mb4;
INSERT INTO voucher (shop_id, title, sub_title, rules, pay_value, actual_value, type, status, stock, begin_time, end_time) VALUES
-- 商铺1的普通券
(1, '满50减10优惠券', '全场通用', '满50元可用，不与其他优惠叠加', 100, 5000, 0, 1, 100, NULL, NULL),
(1, '新用户专享20元券', '仅限新用户', '新用户首单可用', 500, 2000, 0, 1, 50, NULL, NULL),

-- 商铺1的秒杀券（未来7天内有效）
(1, '限时秒杀：5元抢30元券', '限量特惠', '限时抢购，先到先得', 500, 3000, 1, 1, 20, 
 DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY)),

-- 商铺2的普通券
(2, '学子超市8折券', '部分商品可用', '指定商品8折优惠', 1000, 2000, 0, 1, 200, NULL, NULL),

-- 商铺2的秒杀券（已过期，用于测试过期状态）
(2, '双11特惠：1元抢15元券', '活动已结束', '限时特惠', 100, 1500, 1, 3, 0,
 DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 商铺3的普通券
(3, '水果拼盘立减15元', '满80可用', '水果拼盘专用', 1500, 1500, 0, 1, 30, NULL, NULL),

-- 商铺4的秒杀券（即将开始）
(4, '咖啡节特惠：3元抢25元券', '即将开始', '咖啡节限量特惠', 300, 2500, 1, 2, 50,
 DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY)),

-- 商铺5的普通券（下架状态）
(5, '文具满减券', '已下架', '满30减5元', 500, 500, 0, 2, 100, NULL, NULL),

-- 商铺6的秒杀券（正在进行中）
(6, '奶茶买一送一券', '限时抢购', '第二杯半价', 800, 1600, 1, 1, 15,
 DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 DAY));
