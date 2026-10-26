package com.campus.service;

/**
 * 订单超时服务接口
 */
public interface OrderTimeoutService {
    
    /**
     * 调度订单超时取消
     * @param orderId 订单ID
     * @param delayMinutes 延迟时间（分钟）
     */
    void scheduleOrderTimeout(Long orderId, int delayMinutes);
}
