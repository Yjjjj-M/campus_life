package com.campus.controller;

import com.campus.common.BusinessException;
import com.campus.common.Result;
import com.campus.entity.Order;
import com.campus.entity.Product;
import com.campus.interceptor.UserHolder;
import com.campus.mapper.ProductMapper;
import com.campus.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/cart")
@RequiredArgsConstructor
public class UserCartController {
    private final CartService cartService;
    private final ProductMapper productMapper;

    @PostMapping("/add")
    public Result<Void> addToCart(@RequestParam Long productId, 
                                  @RequestParam(defaultValue = "1") Integer quantity) {
        Long userId = UserHolder.getUserId();
        cartService.addToCart(userId, productId, quantity);
        return Result.ok();
    }

    @PutMapping("/update")
    public Result<Void> updateCartItem(@RequestParam Long productId, 
                                       @RequestParam Integer quantity) {
        Long userId = UserHolder.getUserId();
        cartService.updateCartItem(userId, productId, quantity);
        return Result.ok();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> removeFromCart(@PathVariable Long productId) {
        Long userId = UserHolder.getUserId();
        cartService.removeFromCart(userId, productId);
        return Result.ok();
    }

    @DeleteMapping("/clear")
    public Result<Void> clearCart() {
        Long userId = UserHolder.getUserId();
        cartService.clearCart(userId);
        return Result.ok();
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getCart() {
        Long userId = UserHolder.getUserId();
        Map<Long, Integer> cartMap = cartService.getCart(userId);
        
        // 查询商品详情
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> items = new java.util.ArrayList<>();
        int totalPrice = 0;
        
        for (Map.Entry<Long, Integer> entry : cartMap.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            Product product = productMapper.selectById(productId);
            if (product != null && product.getStatus() == 1) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", productId);
                item.put("productName", product.getName());
                item.put("productImage", product.getImage());
                item.put("price", product.getPrice());
                item.put("quantity", quantity);
                item.put("subtotal", product.getPrice() * quantity);
                item.put("stock", product.getStock());
                items.add(item);
                
                totalPrice += product.getPrice() * quantity;
            }
        }
        
        result.put("items", items);
        result.put("totalPrice", totalPrice);
        result.put("totalCount", cartMap.size());
        
        return Result.ok(result);
    }

    @PostMapping("/checkout")
    public Result<Order> checkout(@RequestParam List<Long> productIds,
                                  @RequestParam(required = false) Long couponId) {
        Long userId = UserHolder.getUserId();
        Order order = cartService.checkout(userId, productIds, couponId);
        return Result.ok(order);
    }
}
