-- 商铺点评表
CREATE TABLE review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    shop_id BIGINT NOT NULL COMMENT '商铺ID',
    order_id BIGINT COMMENT '订单ID（可选，关联消费记录）',
    rating TINYINT NOT NULL COMMENT '评分（1-5）',
    content TEXT COMMENT '评价内容',
    images VARCHAR(2000) COMMENT '图片URL列表，逗号分隔',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态（1-正常 0-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_shop_id (shop_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_create_time (create_time)
) COMMENT='商铺点评表';

-- 点赞记录表
CREATE TABLE review_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    review_id BIGINT NOT NULL COMMENT '评价ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_user_review (user_id, review_id),
    INDEX idx_review_id (review_id)
) COMMENT='评价点赞记录表';
