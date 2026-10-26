package com.campus.service;

import com.campus.entity.Order;

import java.util.List;
import java.util.Map;

public interface CartService {
    /**
     * 添加商品到购物车
     */
    void addToCart(Long userId, Long productId, Integer quantity);

    /**
     * 更新购物车商品数量
     */
    void updateCartItem(Long userId, Long productId, Integer quantity);

    /**
     * 删除购物车商品
     */
    void removeFromCart(Long userId, Long productId);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);

    /**
     * 获取购物车列表
     */
    Map<Long, Integer> getCart(Long userId);

    /**
     * 购物车结算
     */
    Order checkout(Long userId, List<Long> productIds, Long couponId);
}
