package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.entity.Order;
import com.campus.entity.Voucher;
import com.campus.mapper.OrderMapper;
import com.campus.mapper.VoucherMapper;
import com.campus.service.OrderTimeoutService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 订单超时服务实现
 * 使用 Redisson 延迟队列实现订单超时自动取消
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTimeoutServiceImpl implements OrderTimeoutService {
    
    private final RedissonClient redissonClient;
    private final OrderMapper orderMapper;
    private final VoucherMapper voucherMapper;
    
    private static final String ORDER_TIMEOUT_QUEUE = "order:timeout:queue";
    private RBlockingQueue<Long> blockingQueue;
    private RDelayedQueue<Long> delayedQueue;
    private Thread listenerThread;
    private volatile boolean running = true;
    
    @PostConstruct
    public void init() {
        // 初始化延迟队列
        blockingQueue = redissonClient.getBlockingQueue(ORDER_TIMEOUT_QUEUE);
        delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        
        // 启动监听线程
        startListener();
        log.info("订单超时监听服务已启动");
    }
    
    @PreDestroy
    public void destroy() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        log.info("订单超时监听服务已停止");
    }
    
    @Override
    public void scheduleOrderTimeout(Long orderId, int delayMinutes) {
        try {
            delayedQueue.offer(orderId, delayMinutes, TimeUnit.MINUTES);
            log.info("已调度订单超时取消：orderId={}, delay={}分钟", orderId, delayMinutes);
        } catch (Exception e) {
            log.error("调度订单超时失败：orderId={}", orderId, e);
        }
    }
    
    /**
     * 启动监听线程，监听延迟队列
     */
    private void startListener() {
        listenerThread = new Thread(() -> {
            while (running) {
                try {
                    // 阻塞等待订单超时
                    Long orderId = blockingQueue.poll(1, TimeUnit.SECONDS);
                    if (orderId != null) {
                        log.info("订单超时，开始处理取消：orderId={}", orderId);
                        cancelOrderIfPending(orderId);
                    }
                } catch (InterruptedException e) {
                    if (running) {
                        log.warn("监听线程被中断", e);
                    }
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("监听订单超时队列异常", e);
                }
            }
        }, "order-timeout-listener");
        
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    /**
     * 如果订单仍为待支付状态，则取消订单
     * 幂等性设计：多次调用结果一致
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderIfPending(Long orderId) {
        try {
            // 1. 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                log.warn("订单不存在：orderId={}", orderId);
                return;
            }
            
            // 2. 检查订单状态（只有待支付状态才需要取消）
            if (order.getStatus() != 1) {
                log.info("订单已处理，跳过取消：orderId={}, status={}", orderId, order.getStatus());
                return;
            }
            
            // 3. 更新订单状态为已取消
            order.setStatus(4); // 4=已取消
            order.setCancelTime(LocalDateTime.now());
            orderMapper.updateById(order);
            log.info("订单已自动取消：orderId={}", orderId);
            
            // 4. 恢复优惠券库存
            LambdaUpdateWrapper<Voucher> restoreWrapper = new LambdaUpdateWrapper<>();
            restoreWrapper.setSql("stock = stock + 1")
                    .eq(Voucher::getId, order.getVoucherId());
            int updated = voucherMapper.update(null, restoreWrapper);
            
            if (updated > 0) {
                log.info("已恢复优惠券库存：voucherId={}", order.getVoucherId());
            } else {
                log.warn("恢复优惠券库存失败：voucherId={}", order.getVoucherId());
            }
            
        } catch (Exception e) {
            log.error("自动取消订单失败：orderId={}", orderId, e);
        }
    }
}
