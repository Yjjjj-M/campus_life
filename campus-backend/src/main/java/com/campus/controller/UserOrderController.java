package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.Order;
import com.campus.interceptor.UserHolder;
import com.campus.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端订单控制器
 */
@Tag(name = "用户端-订单管理")
@RestController
@RequestMapping("/api/user/order")
@RequiredArgsConstructor
public class UserOrderController {
    
    private final OrderService orderService;
    
    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<Order> createOrder(@RequestParam Long voucherId) {
        Long userId = UserHolder.getUserId();
        Order order = orderService.createOrder(userId, voucherId);
        return Result.ok(order);
    }
    
    @Operation(summary = "支付订单")
    @PostMapping("/{orderId}/pay")
    public Result<Void> payOrder(@PathVariable Long orderId) {
        Long userId = UserHolder.getUserId();
        orderService.payOrder(userId, orderId);
        return Result.ok();
    }
    
    @Operation(summary = "取消订单")
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        Long userId = UserHolder.getUserId();
        orderService.cancelOrder(userId, orderId);
        return Result.ok();
    }
    
    @Operation(summary = "查询我的订单列表")
    @GetMapping("/page")
    public Result<PageResult<Order>> pageOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Long userId = UserHolder.getUserId();
        PageResult<Order> result = orderService.pageForUser(userId, page, size, status);
        return Result.ok(result);
    }
    
    @Operation(summary = "查询订单详情")
    @GetMapping("/{orderId}")
    public Result<Order> getOrderDetail(@PathVariable Long orderId) {
        Long userId = UserHolder.getUserId();
        Order order = orderService.getDetailForUser(userId, orderId);
        return Result.ok(order);
    }
}
