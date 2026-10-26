package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.BusinessException;
import com.campus.entity.Order;
import com.campus.entity.OrderDetail;
import com.campus.entity.SeckillVoucher;
import com.campus.entity.Shop;
import com.campus.entity.Voucher;
import com.campus.mapper.OrderDetailMapper;
import com.campus.mapper.SeckillVoucherMapper;
import com.campus.service.OrderService;
import com.campus.service.OrderTimeoutService;
import com.campus.service.ShopService;
import com.campus.service.VoucherService;
import com.campus.utils.RedisIdWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单处理器
 * 监听 Redis Stream，异步创建秒杀订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherOrderHandler {
    
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final VoucherService voucherService;
    private final OrderService orderService;
    private final ShopService shopService;
    private final SeckillVoucherMapper seckillVoucherMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final RedisIdWorker redisIdWorker;
    private final OrderTimeoutService orderTimeoutService;
    
    /** Stream 队列名 */
    private static final String STREAM_KEY = "stream:voucher:order";
    /** 消费组名 */
    private static final String GROUP_NAME = "g1";
    /** 消费者名 */
    private static final String CONSUMER_NAME = "c1";
    /** 分布式锁前缀 */
    private static final String LOCK_PREFIX = "lock:voucher:order:";
    
    /** 订单状态常量 */
    private static final int STATUS_PENDING = 1;
    
    /** 处理线程（单线程保证顺序处理） */
    private ExecutorService executorService;
    
    /**
     * 启动监听线程
     */
    @PostConstruct
    public void init() {
        // 确保 Stream 和消费组存在
        try {
            // 先检查 Stream 是否存在，如果不存在则添加一个占位消息
            if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(STREAM_KEY))) {
                stringRedisTemplate.opsForStream().add(
                    STREAM_KEY, 
                    Map.of("placeholder", "1")
                );
                log.info("创建 Stream：{}", STREAM_KEY);
            }
            
            // 创建消费组（如果不存在）
            try {
                stringRedisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
                log.info("创建 Stream 消费组：{}", GROUP_NAME);
            } catch (Exception e) {
                // 消费组已存在，忽略
                log.debug("Stream 消费组已存在：{}", GROUP_NAME);
            }
        } catch (Exception e) {
            log.error("初始化 Stream 消费组失败", e);
        }
        
        // 启动单线程处理消息
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "seckill-order-handler");
            t.setDaemon(true);
            return t;
        });
        executorService.submit(this::handlePendingList);
        
        // 启动另一个线程处理 pending 列表（处理失败的消息）
        executorService.submit(this::handleFailedMessages);
        
        log.info("秒杀订单处理器启动完成");
    }
    
    /**
     * 关闭线程池
     */
    @PreDestroy
    public void destroy() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
    
    /**
     * 主线程：持续从 Stream 读取新消息
     */
    private void handlePendingList() {
        while (true) {
            try {
                // 阻塞读取新消息（最多等待 2 秒）
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from(GROUP_NAME, CONSUMER_NAME),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                );
                
                if (list == null || list.isEmpty()) {
                    continue;
                }
                
                // 处理消息
                MapRecord<String, Object, Object> record = list.get(0);
                handleVoucherOrder(record);
                
            } catch (Exception e) {
                log.error("处理秒杀订单失败", e);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    /**
     * 处理失败的消息（pending list）
     */
    private void handleFailedMessages() {
        while (true) {
            try {
                // 从 pending list 读取失败消息
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from(GROUP_NAME, CONSUMER_NAME),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(STREAM_KEY, ReadOffset.from("0"))
                );
                
                if (list == null || list.isEmpty()) {
                    Thread.sleep(2000);
                    continue;
                }
                
                MapRecord<String, Object, Object> record = list.get(0);
                handleVoucherOrder(record);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("处理失败消息异常", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    /**
     * 处理单个订单消息
     */
    private void handleVoucherOrder(MapRecord<String, Object, Object> record) {
        Map<Object, Object> value = record.getValue();
        Long voucherId = Long.valueOf(value.get("voucherId").toString());
        Long userId = Long.valueOf(value.get("userId").toString());
        
        // 获取分布式锁（防止 Stream 重试导致重复下单）
        RLock lock = redissonClient.getLock(LOCK_PREFIX + userId + ":" + voucherId);
        boolean isLock = lock.tryLock();
        
        if (!isLock) {
            // 获取锁失败，说明已有其他线程在处理
            log.warn("获取锁失败：userId={}, voucherId={}", userId, voucherId);
            return;
        }
        
        try {
            // 创建订单
            createVoucherOrder(userId, voucherId);
            
            // ACK 确认消费
            stringRedisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, record.getId());
            log.info("秒杀订单创建成功：userId={}, voucherId={}, recordId={}", 
                    userId, voucherId, record.getId());
            
        } catch (Exception e) {
            log.error("创建秒杀订单失败：userId={}, voucherId={}", userId, voucherId, e);
            // 不 ACK，消息会进入 pending list，由 handleFailedMessages 重试
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 创建秒杀订单（数据库操作）
     * 优化：优惠券信息从 Redis Hash 读取，减少数据库查询
     */
    private void createVoucherOrder(Long userId, Long voucherId) {
        // 1. 检查是否已经创建过订单（防止重复下单）
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getVoucherId, voucherId);
        Long count = orderService.count(wrapper);
        if (count > 0) {
            log.warn("用户重复下单，跳过：userId={}, voucherId={}", userId, voucherId);
            return; // 已经下过单，直接返回（算成功）
        }
        
        // 2. 扣减库存（乐观锁）
        int updated = seckillVoucherMapper.deductStock(voucherId);
        if (updated == 0) {
            throw new BusinessException(400, "库存不足");
        }
        
        // 2. ⭐ 从 Redis 缓存读取优惠券信息（避免查数据库）
        String infoKey = "seckill:info:" + voucherId;
        Map<Object, Object> voucherCache = stringRedisTemplate.opsForHash().entries(infoKey);
        
        Long shopId;
        Integer payValue;
        Integer actualValue;
        String title;
        String subTitle;
        String rules;
        
        if (!voucherCache.isEmpty()) {
            // 从 Redis Hash 读取
            shopId = Long.valueOf(voucherCache.get("shopId").toString());
            payValue = Integer.valueOf(voucherCache.get("payValue").toString());
            actualValue = Integer.valueOf(voucherCache.get("actualValue").toString());
            title = voucherCache.get("title").toString();
            subTitle = voucherCache.getOrDefault("subTitle", "").toString();
            rules = voucherCache.getOrDefault("rules", "").toString();
        } else {
            // 缓存未命中，回退到数据库
            Voucher voucher = voucherService.getById(voucherId);
            if (voucher == null) {
                seckillVoucherMapper.restoreStock(voucherId);
                throw new BusinessException(400, "优惠券不存在");
            }
            shopId = voucher.getShopId();
            payValue = voucher.getPayValue();
            actualValue = voucher.getActualValue();
            title = voucher.getTitle();
            subTitle = voucher.getSubTitle();
            rules = voucher.getRules();
        }
        
        // 3. 查询商铺信息
        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            seckillVoucherMapper.restoreStock(voucherId);
            throw new BusinessException(400, "商铺不存在");
        }
        
        // 4. 生成订单 ID（使用 RedisIdWorker）
        Long orderId = redisIdWorker.nextId("order");
        
        // 5. 生成订单号
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        String orderNo = timestamp + random;
        
        // 6. 创建订单
        Order order = new Order();
        order.setId(orderId);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        order.setShopId(shopId);
        order.setStatus(STATUS_PENDING);
        order.setPayValue(payValue);
        order.setActualValue(actualValue);
        order.setCreateTime(LocalDateTime.now());
        orderService.save(order);
        
        // 7. 创建订单明细
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setVoucherTitle(title);
        detail.setVoucherSubTitle(subTitle);
        detail.setShopName(shop.getName());
        detail.setShopAddress(shop.getAddress());
        detail.setPayValue(payValue);
        detail.setActualValue(actualValue);
        detail.setRules(rules);
        orderDetailMapper.insert(detail);
        
        // 8. 调度订单超时取消（1 分钟后自动取消）
        orderTimeoutService.scheduleOrderTimeout(orderId, 1);
        
        log.info("秒杀订单创建完成：orderId={}, orderNo={}, userId={}, voucherId={}", 
                orderId, orderNo, userId, voucherId);
    }
}
