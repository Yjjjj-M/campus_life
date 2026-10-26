package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.DashboardStats;
import com.campus.entity.Order;
import com.campus.entity.Shop;
import com.campus.entity.User;
import com.campus.mapper.OrderMapper;
import com.campus.mapper.ShopMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Dashboard 统计服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final ShopMapper shopMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    
    @Override
    public DashboardStats getStats() {
        // 统计商铺总数
        Long shopCount = shopMapper.selectCount(null);
        
        // 统计用户总数
        Long userCount = userMapper.selectCount(null);
        
        // 统计订单总数
        Long orderCount = orderMapper.selectCount(null);
        
        // 统计今日订单数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayOrderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .ge(Order::getCreateTime, todayStart)
        );
        
        // 统计今日销售额（已支付订单）
        LambdaQueryWrapper<Order> revenueWrapper = new LambdaQueryWrapper<>();
        revenueWrapper.ge(Order::getCreateTime, todayStart)
                .in(Order::getStatus, 2, 3); // 已支付或已使用
        
        Long todayRevenue = 0L;
        for (Order order : orderMapper.selectList(revenueWrapper)) {
            todayRevenue += order.getPayValue();
        }
        
        return DashboardStats.builder()
                .shopCount(shopCount)
                .userCount(userCount)
                .orderCount(orderCount)
                .todayOrderCount(todayOrderCount)
                .todayRevenue(todayRevenue)
                .build();
    }
}
