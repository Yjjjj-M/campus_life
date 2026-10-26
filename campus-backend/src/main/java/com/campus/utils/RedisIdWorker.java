package com.campus.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Redis 全局唯一 ID 生成器
 * 
 * ID 结构：时间戳（32 bit）+ Redis 自增序列（32 bit）= 64 bit long
 * 
 * 时间戳：当前时间 - 起始时间（2022-01-01）的秒数
 * 序列：每天一个 key，每天从 0 开始自增，避免单 key 过大
 * 
 * 优点：
 * 1. 唯一性（时间戳 + 序列保证）
 * 2. 递增性（时间戳递增）
 * 3. 高性能（Redis INCR 单线程原子操作）
 */
@Component
public class RedisIdWorker {
    
    /**
     * 起始时间戳：2022-01-01 00:00:00
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    
    /**
     * 序列号的位数（32 bit，可以支持每天 42 亿订单）
     */
    private static final int COUNT_BITS = 32;
    
    private final StringRedisTemplate stringRedisTemplate;
    
    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    /**
     * 生成全局唯一 ID
     * @param keyPrefix key 前缀，例如 "order"
     * @return 唯一 ID
     */
    public long nextId(String keyPrefix) {
        // 1. 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        
        // 2. 生成序列号（每天一个 key，例如 icr:order:2026:01:20）
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        
        // 3. 拼接并返回
        // 高 32 位 = 时间戳，低 32 位 = 序列号
        return timestamp << COUNT_BITS | (count == null ? 0L : count);
    }
}
