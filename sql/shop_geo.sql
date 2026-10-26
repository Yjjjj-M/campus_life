-- 给商铺表添加经纬度字段
ALTER TABLE shop ADD COLUMN longitude DOUBLE COMMENT '经度' AFTER address;
ALTER TABLE shop ADD COLUMN latitude DOUBLE COMMENT '纬度' AFTER longitude;

-- 更新现有商铺的经纬度（以北京大学为中心，分布在周围）
UPDATE shop SET longitude = 116.310003, latitude = 39.991957 WHERE id = 1;
UPDATE shop SET longitude = 116.310503, latitude = 39.992157 WHERE id = 2;
UPDATE shop SET longitude = 116.309503, latitude = 39.991757 WHERE id = 3;
UPDATE shop SET longitude = 116.311003, latitude = 39.992357 WHERE id = 4;
UPDATE shop SET longitude = 116.309003, latitude = 39.991557 WHERE id = 5;

-- 如果有更多商铺，继续添加
-- UPDATE shop SET longitude = 116.310803, latitude = 39.992257 WHERE id = 6;
-- UPDATE shop SET longitude = 116.309203, latitude = 39.991657 WHERE id = 7;
