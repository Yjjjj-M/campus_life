package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.common.PageResult;
import com.campus.entity.Order;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {
    
    /**
     * 用户创建订单
     * @param userId 用户ID
     * @param voucherId 优惠券ID
     * @return 订单信息
     */
    Order createOrder(Long userId, Long voucherId);
    
    /**
     * 用户支付订单
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    void payOrder(Long userId, Long orderId);
    
    /**
     * 用户取消订单
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    void cancelOrder(Long userId, Long orderId);
    
    /**
     * 管理员核销订单（使用）
     * @param orderId 订单ID
     */
    void useOrder(Long orderId);
    
    /**
     * 用户查询自己的订单列表（分页）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选（可选）
     * @return 分页结果
     */
    PageResult<Order> pageForUser(Long userId, int page, int size, Integer status);
    
    /**
     * 用户查询订单详情
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 订单信息
     */
    Order getDetailForUser(Long userId, Long orderId);
    
    /**
     * 管理员分页查询所有订单
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选（可选）
     * @param orderNo 订单号搜索（可选）
     * @param userPhone 用户手机号搜索（可选）
     * @return 分页结果
     */
    PageResult<Order> pageForAdmin(int page, int size, Integer status, String orderNo, String userPhone);
    
    /**
     * 管理员查询订单详情
     * @param orderId 订单ID
     * @return 订单信息
     */
    Order getDetailForAdmin(Long orderId);
}
