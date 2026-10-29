-- ==========================================
-- 校园生活平台 - 完整示例数据
-- 包含：分类、商铺、商品、优惠券、评价
-- ==========================================

-- 1. 清空旧数据
DELETE FROM review;
DELETE FROM order_detail;
DELETE FROM order_item;
DELETE FROM orders;
DELETE FROM seckill_voucher;
DELETE FROM voucher;
DELETE FROM product;
DELETE FROM shop;
DELETE FROM shop_category;

-- 2. 商铺分类
INSERT INTO shop_category (id, name, icon, sort, status) VALUES
(1, '美食', '🍜', 1, 1),
(2, '甜品', '🍰', 2, 1),
(3, '咖啡', '☕', 3, 1),
(4, '奶茶', '🧋', 4, 1),
(5, '快餐', '🍔', 5, 1),
(6, '水果', '🍎', 6, 1);

-- 3. 商铺数据（12家，使用 Unsplash 真实图片）
INSERT INTO shop (id, name, category_id, images, area, address, avg_price, score, sold, status, open_hours, longitude, latitude) VALUES
(1, '老四川麻辣烫', 1, 
 'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800,https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800',
 '南区', '校园南区商业街 A01', 2500, 47, 1580, 1, '10:00-22:00', 116.310000, 39.990000),

(2, '鲜果时光', 6,
 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=800,https://images.unsplash.com/photo-1619566636858-adf3ef464007?w=800',
 '北区', '校园北区水果街 B05', 1800, 45, 2340, 1, '08:00-21:00', 116.312000, 39.992000),

(3, '星巴克咖啡', 3,
 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800,https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800',
 '东区', '图书馆一层东侧', 4500, 48, 3890, 1, '07:30-22:30', 116.315000, 39.991000),

(4, '一点点奶茶', 4,
 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=800,https://images.unsplash.com/photo-1571934811356-5cc061b6821f?w=800',
 '西区', '教学楼 B 座一层', 1500, 46, 5120, 1, '09:00-22:00', 116.308000, 39.989000),

(5, '汉堡王', 5,
 'https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=800,https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800',
 '南区', '校园南区商业街 A05', 3500, 44, 4230, 1, '10:00-23:00', 116.310500, 39.990500),

(6, '甜蜜时光烘焙坊', 2,
 'https://images.unsplash.com/photo-1517433670267-08bbd4be890f?w=800,https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800',
 '北区', '校园北区商业街 C02', 2800, 49, 1890, 1, '08:00-21:30', 116.312500, 39.992500),

(7, '拉面道场', 1,
 'https://images.unsplash.com/photo-1557872943-16a5ac26437e?w=800,https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800',
 '东区', '校园东区商业街 D03', 3200, 46, 2780, 1, '11:00-22:00', 116.315500, 39.991500),

(8, '茶颜悦色', 4,
 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=800,https://images.unsplash.com/photo-1571934811356-5cc061b6821f?w=800',
 '西区', '校园西区广场', 1800, 48, 6540, 1, '09:30-22:00', 116.308500, 39.989500),

(9, '必胜客', 5,
 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800,https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800',
 '南区', '南门商业街 E01', 5800, 43, 3120, 1, '10:00-22:00', 116.310200, 39.990200),

(10, '瑞幸咖啡', 3,
 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800,https://images.unsplash.com/photo-1509042239860-f550b710b6bd?w=800',
 '东区', '图书馆 B 层', 2500, 47, 8900, 1, '07:00-22:00', 116.315200, 39.991200),

(11, '满记甜品', 2,
 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=800,https://images.unsplash.com/photo-1488477181946-6428a0291777?w=800',
 '北区', '北区商业街 C05', 4200, 48, 2340, 1, '10:00-22:30', 116.312800, 39.992800),

(12, '黄焖鸡米饭', 5,
 'https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=800,https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800',
 '西区', '西区商业街 F02', 2000, 45, 4560, 1, '10:30-21:30', 116.308200, 39.989200);

-- 4. 商品数据（每个商铺 3-4 个商品）
INSERT INTO product (shop_id, category_id, name, price, image, description, stock, status, sales) VALUES
-- 老四川麻辣烫
(1, 1, '经典麻辣烫', 2500, 'https://images.unsplash.com/photo-1617093727343-374698b1b08d?w=400', '精选时蔬 + 肉丸 + 豆腐，地道四川味', 100, 1, 890),
(1, 1, '冒菜套餐', 3200, 'https://images.unsplash.com/photo-1585032226651-759b368d7246?w=400', '秘制汤底 + 丰富配菜', 80, 1, 420),
(1, 1, '酸辣粉', 1800, 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400', '正宗重庆酸辣粉', 120, 1, 560),

-- 鲜果时光
(2, 6, '精品果盘', 2800, 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=400', '当季新鲜水果拼盘', 60, 1, 780),
(2, 6, '鲜榨果汁', 1500, 'https://images.unsplash.com/photo-1622597467836-f3285f2131b8?w=400', '现榨当季水果', 100, 1, 1230),
(2, 6, '进口车厘子', 6800, 'https://images.unsplash.com/photo-1528825871115-3581a5387919?w=400', '智利进口 JJ 级', 30, 1, 156),

-- 星巴克
(3, 3, '美式咖啡', 2800, 'https://images.unsplash.com/photo-1509042239860-f550b710b6bd?w=400', '经典意式浓缩 + 热水', 200, 1, 2100),
(3, 3, '拿铁', 3200, 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400', '浓缩咖啡 + 蒸汽牛奶', 180, 1, 1890),
(3, 3, '星冰乐', 3800, 'https://images.unsplash.com/photo-1497636577773-f2c897983eba?w=400', '冰沙咖啡，夏日必备', 150, 1, 980),

-- 一点点奶茶
(4, 4, '珍珠奶茶', 1200, 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=400', '经典台式珍珠奶茶', 200, 1, 3420),
(4, 4, '四季春茶', 1000, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400', '清香回甘', 180, 1, 1890),
(4, 4, '波霸奶绿', 1500, 'https://images.unsplash.com/photo-1571934811356-5cc061b6821f?w=400', '奶香浓郁', 160, 1, 2560),

-- 汉堡王
(5, 5, '皇堡', 3500, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400', '经典牛肉汉堡', 100, 1, 1890),
(5, 5, '香辣鸡腿堡', 2500, 'https://images.unsplash.com/photo-1606755962773-d324e0a13086?w=400', '香脆鸡腿 + 生菜', 120, 1, 2340),
(5, 5, '薯条大份', 1500, 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400', '金黄酥脆', 200, 1, 4560),

-- 甜蜜时光
(6, 2, '提拉米苏', 3800, 'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400', '意式经典甜品', 50, 1, 670),
(6, 2, '法式可颂', 1800, 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400', '酥脆黄油香气', 80, 1, 890),
(6, 2, '草莓慕斯', 3200, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400', '新鲜草莓 + 慕斯', 60, 1, 540),

-- 拉面道场
(7, 1, '豚骨拉面', 3200, 'https://images.unsplash.com/photo-1557872943-16a5ac26437e?w=400', '浓郁猪骨汤底', 90, 1, 1560),
(7, 1, '味噌拉面', 3500, 'https://images.unsplash.com/photo-1591777688247-0b61d2d2b45f?w=400', '日式味噌风味', 85, 1, 890),
(7, 1, '炸鸡块', 1800, 'https://images.unsplash.com/photo-1562967914-608f82629710?w=400', '香脆多汁', 120, 1, 1230),

-- 茶颜悦色
(8, 4, '幽兰拿铁', 1800, 'https://images.unsplash.com/photo-1541167760496-1628856ab772?w=400', '招牌奶茶', 200, 1, 4320),
(8, 4, '声声乌龙', 1600, 'https://images.unsplash.com/photo-1571934811356-5cc061b6821f?w=400', '乌龙茶底', 180, 1, 2890),
(8, 4, '桂花弄', 1500, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400', '桂花清香', 160, 1, 1670),

-- 必胜客
(9, 5, '超级至尊披萨', 8800, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400', '丰富配料，芝士拉丝', 60, 1, 1890),
(9, 5, '夏威夷披萨', 6800, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400', '火腿 + 菠萝', 70, 1, 1230),
(9, 5, '奶油蘑菇汤', 2500, 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400', '浓郁奶香', 100, 1, 780),

-- 瑞幸咖啡
(10, 3, '生椰拿铁', 2900, 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400', '网红爆款', 200, 1, 5670),
(10, 3, '厚乳拿铁', 2800, 'https://images.unsplash.com/photo-1509042239860-f550b710b6bd?w=400', '浓郁奶香', 180, 1, 4320),
(10, 3, '陨石拿铁', 3200, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', '巧克力风味', 160, 1, 2340),

-- 满记甜品
(11, 2, '杨枝甘露', 3800, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400', '经典港式甜品', 80, 1, 1450),
(11, 2, '芒果班戟', 3200, 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400', '新鲜芒果 + 奶油', 90, 1, 1230),
(11, 2, '双皮奶', 2500, 'https://images.unsplash.com/photo-1517433670267-08bbd4be890f?w=400', '香滑奶香', 120, 1, 1890),

-- 黄焖鸡米饭
(12, 5, '黄焖鸡米饭', 2200, 'https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=400', '鲜嫩鸡肉 + 土豆', 150, 1, 3450),
(12, 5, '黄焖排骨饭', 2800, 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400', '香浓排骨', 120, 1, 1890),
(12, 5, '黄焖牛肉饭', 3200, 'https://images.unsplash.com/photo-1574484284002-952d92456975?w=400', '大块牛肉', 100, 1, 1230);

-- 5. 优惠券数据
INSERT INTO voucher (shop_id, title, sub_title, rules, pay_value, actual_value, type, status, stock, begin_time, end_time) VALUES
-- 普通券 type=0
(1, '满50减10元优惠券', '麻辣烫专享', '满50元可用', 1000, 5000, 0, 1, 100, NULL, NULL),
(1, '新人专享20元券', '首次下单立减', '无门槛使用', 2000, 5000, 0, 1, 50, NULL, NULL),
(2, '鲜果8折券', '全场8折', '满30元可用', 800, 1000, 0, 1, 200, NULL, NULL),
(3, '星巴克满赠券', '满2杯送1杯', '购买2杯及以上', 0, 3800, 0, 1, 100, NULL, NULL),
(4, '奶茶立减5元', '全场可用', '无门槛', 500, 1500, 0, 1, 300, NULL, NULL),
(5, '汉堡王套餐券', '汉堡+薯条+饮料', '仅限套餐', 3500, 5500, 0, 1, 150, NULL, NULL),
(6, '甜品9折券', '全场9折', '满20元可用', 900, 1000, 0, 1, 180, NULL, NULL),
(7, '拉面免费加面', '加量不加价', '限拉面商品', 0, 500, 0, 1, 200, NULL, NULL),
(8, '茶颜第二杯半价', '两杯起购', '仅限同款', 750, 1800, 0, 1, 250, NULL, NULL),

-- 秒杀券 type=1
(1, '【秒杀】满100减50', '限时秒杀', '满100元可用', 5000, 10000, 1, 1, 200, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(3, '【秒杀】咖啡买一送一', '限时秒杀', '仅限美式/拿铁', 2800, 5600, 1, 1, 100, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(4, '【秒杀】奶茶1元购', '限时秒杀', '仅限珍珠奶茶', 100, 1200, 1, 1, 50, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(6, '【秒杀】甜品半价', '限时秒杀', '全场适用', 1900, 3800, 1, 1, 100, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(10, '【秒杀】瑞幸咖啡9.9元', '限时秒杀', '生椰拿铁专享', 990, 2900, 1, 1, 200, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

-- 6. 秒杀库存表
INSERT INTO seckill_voucher (voucher_id, stock, begin_time, end_time) VALUES
(10, 200, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(11, 100, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(12, 50, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(13, 100, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(14, 200, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));
