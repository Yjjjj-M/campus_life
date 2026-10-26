-- 商品分类表
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sort (sort)
) COMMENT='商品分类表';

-- 商品表
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    shop_id BIGINT NOT NULL COMMENT '商铺ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    price INT NOT NULL COMMENT '价格（分）',
    image VARCHAR(255) COMMENT '图片URL',
    description TEXT COMMENT '商品描述',
    stock INT DEFAULT 100 COMMENT '库存',
    status TINYINT DEFAULT 1 COMMENT '状态 1上架 0下架',
    sales INT DEFAULT 0 COMMENT '销量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_shop_id (shop_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) COMMENT='商品表';

-- 插入测试数据
INSERT INTO product_category (name, sort, status) VALUES
('奶茶饮品', 1, 1),
('咖啡', 2, 1),
('快餐', 3, 1),
('小吃', 4, 1),
('甜品', 5, 1);
