package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.common.ErrorCode;
import com.campus.common.PageResult;
import com.campus.entity.*;
import com.campus.mapper.OrderDetailMapper;
import com.campus.mapper.OrderMapper;
import com.campus.service.OrderService;
import com.campus.service.OrderTimeoutService;
import com.campus.service.ShopService;
import com.campus.service.UserService;
import com.campus.service.VoucherService;
import com.campus.utils.RedisIdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    private final OrderDetailMapper orderDetailMapper;
    private final VoucherService voucherService;
    private final ShopService shopService;
    private final UserService userService;
    private final OrderTimeoutService orderTimeoutService;
    private final RedisIdWorker redisIdWorker;
    
    /** 订单状态常量 */
    private static final int STATUS_PENDING = 1;    // 待支付
    private static final int STATUS_PAID = 2;       // 已支付
    private static final int STATUS_USED = 3;       // 已使用
    private static final int STATUS_CANCELLED = 4;  // 已取消
    private static final int STATUS_REFUNDED = 5;   // 已退款
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long voucherId) {
        // 1. 验证优惠券存在且上架
        Voucher voucher = voucherService.getById(voucherId);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在");
        }
        if (voucher.getStatus() != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "优惠券已下架或过期");
        }
        
        // 2. 检查库存
        if (voucher.getStock() <= 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "优惠券库存不足");
        }
        
        // 3. 扣减库存（使用乐观锁）
        LambdaUpdateWrapper<Voucher> deductWrapper = new LambdaUpdateWrapper<>();
        deductWrapper.setSql("stock = stock - 1")
                .eq(Voucher::getId, voucherId)
                .gt(Voucher::getStock, 0);
        boolean stockUpdated = voucherService.update(deductWrapper);
        
        if (!stockUpdated) {
            throw new BusinessException(ErrorCode.CONFLICT, "优惠券库存不足，请重试");
        }
        
        // 4. 获取商铺信息
        Shop shop = shopService.getById(voucher.getShopId());
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商铺不存在");
        }
        
        // 5. 创建订单
        Order order = new Order();
        order.setId(redisIdWorker.nextId("order"));
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        order.setShopId(voucher.getShopId());
        order.setStatus(STATUS_PENDING);
        order.setPayValue(voucher.getPayValue());
        order.setActualValue(voucher.getActualValue());
        this.save(order);
        
        // 6. 创建订单明细
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(order.getId());
        detail.setVoucherTitle(voucher.getTitle());
        detail.setVoucherSubTitle(voucher.getSubTitle());
        detail.setShopName(shop.getName());
        detail.setShopAddress(shop.getAddress());
        detail.setPayValue(voucher.getPayValue());
        detail.setActualValue(voucher.getActualValue());
        detail.setRules(voucher.getRules());
        orderDetailMapper.insert(detail);
        
        // 7. 调度订单超时取消（15分钟后自动取消）
        orderTimeoutService.scheduleOrderTimeout(order.getId(), 15);
        
        log.info("订单创建成功：orderNo={}, userId={}, voucherId={}", 
                order.getOrderNo(), userId, voucherId);
        
        return order;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId) {
        // 1. 查询订单
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        
        // 2. 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }
        
        // 3. 验证订单状态
        if (order.getStatus() != STATUS_PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许支付");
        }
        
        // 4. 更新订单状态为已支付
        order.setStatus(STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);
        
        log.info("订单支付成功：orderId={}, orderNo={}", orderId, order.getOrderNo());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        // 1. 查询订单
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        
        // 2. 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }
        
        // 3. 验证订单状态（只有待支付可以取消）
        if (order.getStatus() != STATUS_PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许取消");
        }
        
        // 4. 更新订单状态为已取消
        order.setStatus(STATUS_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        this.updateById(order);
        
        // 5. 恢复库存
        LambdaUpdateWrapper<Voucher> restoreWrapper = new LambdaUpdateWrapper<>();
        restoreWrapper.setSql("stock = stock + 1")
                .eq(Voucher::getId, order.getVoucherId());
        boolean stockRestored = voucherService.update(restoreWrapper);
        
        if (!stockRestored) {
            log.warn("恢复库存失败：voucherId={}", order.getVoucherId());
        }
        
        log.info("订单取消成功：orderId={}, orderNo={}", orderId, order.getOrderNo());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useOrder(Long orderId) {
        // 1. 查询订单
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        
        // 2. 验证订单状态（只有已支付可以核销）
        if (order.getStatus() != STATUS_PAID) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许核销");
        }
        
        // 3. 更新订单状态为已使用
        order.setStatus(STATUS_USED);
        order.setUseTime(LocalDateTime.now());
        this.updateById(order);
        
        log.info("订单核销成功：orderId={}, orderNo={}", orderId, order.getOrderNo());
    }
    
    @Override
    public PageResult<Order> pageForUser(Long userId, int page, int size, Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        
        Page<Order> result = this.page(pageParam, wrapper);
        
        // 填充展示信息
        fillOrderDisplayInfo(result.getRecords());
        
        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }
    
    @Override
    public Order getDetailForUser(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此订单");
        }
        
        // 填充展示信息
        fillOrderDisplayInfo(List.of(order));
        
        return order;
    }
    
    @Override
    public PageResult<Order> pageForAdmin(int page, int size, Integer status, String orderNo, String userPhone) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(status != null, Order::getStatus, status)
                .like(orderNo != null && !orderNo.trim().isEmpty(), Order::getOrderNo, orderNo)
                .orderByDesc(Order::getCreateTime);
        
        // 如果有用户手机号筛选，先查询用户ID
        if (userPhone != null && !userPhone.trim().isEmpty()) {
            List<Long> userIds = userService.list(
                    new LambdaQueryWrapper<User>()
                            .like(User::getPhone, userPhone)
                            .select(User::getId)
            ).stream().map(User::getId).collect(Collectors.toList());
            
            if (userIds.isEmpty()) {
                return new PageResult<>(List.of(), 0, page, size);
            }
            wrapper.in(Order::getUserId, userIds);
        }
        
        Page<Order> result = this.page(pageParam, wrapper);
        
        // 填充展示信息
        fillOrderDisplayInfo(result.getRecords());
        
        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }
    
    @Override
    public Order getDetailForAdmin(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        
        // 填充展示信息
        fillOrderDisplayInfo(List.of(order));
        
        return order;
    }
    
    /**
     * 生成订单号：时间戳 + 随机数
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return timestamp + random;
    }
    
    /**
     * 填充订单展示信息（优惠券标题、商铺名称、用户信息）
     */
    private void fillOrderDisplayInfo(List<Order> orders) {
        if (orders == null || orders.isEmpty()) return;
        
        // 收集所有 voucherId、shopId、userId
        List<Long> voucherIds = orders.stream()
                .map(Order::getVoucherId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> shopIds = orders.stream()
                .map(Order::getShopId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> userIds = orders.stream()
                .map(Order::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询
        Map<Long, String> voucherTitleMap = voucherService.listByIds(voucherIds)
                .stream()
                .collect(Collectors.toMap(Voucher::getId, Voucher::getTitle, (a, b) -> a));
        
        Map<Long, String> shopNameMap = shopService.listByIds(shopIds)
                .stream()
                .collect(Collectors.toMap(Shop::getId, Shop::getName, (a, b) -> a));
        
        Map<Long, User> userMap = userService.listByIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        
        // 填充信息
        for (Order order : orders) {
            order.setVoucherTitle(voucherTitleMap.getOrDefault(order.getVoucherId(), "已删除"));
            order.setShopName(shopNameMap.getOrDefault(order.getShopId(), "已删除"));
            
            User user = userMap.get(order.getUserId());
            if (user != null) {
                order.setUserName(user.getNickname());
                order.setUserPhone(user.getPhone());
            } else {
                order.setUserName("未知用户");
                order.setUserPhone("");
            }
        }
    }
}
