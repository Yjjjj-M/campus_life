package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.Order;
import com.campus.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端订单控制器
 */
@Tag(name = "管理端-订单管理")
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {
    
    private final OrderService orderService;
    
    @Operation(summary = "分页查询所有订单")
    @GetMapping("/page")
    public Result<PageResult<Order>> pageOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String userPhone) {
        PageResult<Order> result = orderService.pageForAdmin(page, size, status, orderNo, userPhone);
        return Result.ok(result);
    }
    
    @Operation(summary = "查询订单详情")
    @GetMapping("/{orderId}")
    public Result<Order> getOrderDetail(@PathVariable Long orderId) {
        Order order = orderService.getDetailForAdmin(orderId);
        return Result.ok(order);
    }
    
    @Operation(summary = "核销订单")
    @PostMapping("/{orderId}/use")
    public Result<Void> useOrder(@PathVariable Long orderId) {
        orderService.useOrder(orderId);
        return Result.ok();
    }
}
