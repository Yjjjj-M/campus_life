package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.common.BusinessException;
import com.campus.entity.Order;
import com.campus.entity.OrderItem;
import com.campus.entity.Product;
import com.campus.entity.Voucher;
import com.campus.mapper.OrderItemMapper;
import com.campus.mapper.OrderMapper;
import com.campus.mapper.ProductMapper;
import com.campus.mapper.VoucherMapper;
import com.campus.service.CartService;
import com.campus.utils.RedisIdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final VoucherMapper voucherMapper;
    private final RedisIdWorker redisIdWorker;

    private static final String CART_KEY_PREFIX = "cart:";

    @Override
    public void addToCart(Long userId, Long productId, Integer quantity) {
        // 1. 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException(400, "商品不存在或已下架");
        }

        // 2. 检查库存
        if (product.getStock() < quantity) {
            throw new BusinessException(400, "库存不足");
        }

        // 3. 添加到购物车（使用 HINCRBY 原子操作）
        String cartKey = CART_KEY_PREFIX + userId;
        redisTemplate.opsForHash().increment(cartKey, productId.toString(), quantity);
    }

    @Override
    public void updateCartItem(Long userId, Long productId, Integer quantity) {
        String cartKey = CART_KEY_PREFIX + userId;
        
        if (quantity <= 0) {
            // 数量为 0 或负数，删除商品
            redisTemplate.opsForHash().delete(cartKey, productId.toString());
        } else {
            // 检查库存
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException(400, "商品不存在");
            }
            if (product.getStock() < quantity) {
                throw new BusinessException(400, "库存不足");
            }
            
            // 更新数量
            redisTemplate.opsForHash().put(cartKey, productId.toString(), quantity);
        }
    }

    @Override
    public void removeFromCart(Long userId, Long productId) {
        String cartKey = CART_KEY_PREFIX + userId;
        redisTemplate.opsForHash().delete(cartKey, productId.toString());
    }

    @Override
    public void clearCart(Long userId) {
        String cartKey = CART_KEY_PREFIX + userId;
        redisTemplate.delete(cartKey);
    }

    @Override
    public Map<Long, Integer> getCart(Long userId) {
        String cartKey = CART_KEY_PREFIX + userId;
        Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(cartKey);
        
        // 转换类型
        Map<Long, Integer> result = new java.util.HashMap<>();
        for (Map.Entry<Object, Object> entry : cartMap.entrySet()) {
            Long productId = Long.parseLong(entry.getKey().toString());
            Integer quantity = Integer.parseInt(entry.getValue().toString());
            result.put(productId, quantity);
        }
        
        return result;
    }

    @Override
    @Transactional
    public Order checkout(Long userId, List<Long> productIds, Long couponId) {
        // 1. 获取购物车商品
        String cartKey = CART_KEY_PREFIX + userId;
        Map<Object, Object> cartItems = redisTemplate.opsForHash().entries(cartKey);
        
        if (cartItems.isEmpty()) {
            throw new BusinessException(400, "购物车为空");
        }

        // 2. 计算总价并创建订单明细
        int totalPrice = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Long productId : productIds) {
            Object quantityObj = cartItems.get(productId.toString());
            if (quantityObj == null) {
                throw new BusinessException(400, "商品 " + productId + " 不在购物车中");
            }
            
            Integer quantity = Integer.parseInt(quantityObj.toString());
            Product product = productMapper.selectById(productId);
            
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException(400, "商品 " + productId + " 不存在或已下架");
            }
            
            if (product.getStock() < quantity) {
                throw new BusinessException(400, "商品 " + product.getName() + " 库存不足");
            }

            int subtotal = product.getPrice() * quantity;
            totalPrice += subtotal;

            OrderItem item = new OrderItem();
            item.setProductId(productId);
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            item.setSubtotal(subtotal);
            orderItems.add(item);
        }

        // 3. 应用优惠券
        int discount = 0;
        if (couponId != null) {
            Voucher voucher = voucherMapper.selectById(couponId);
            if (voucher != null && voucher.getStatus() == 1) {
                discount = voucher.getActualValue() - voucher.getPayValue();
                totalPrice -= discount;
                if (totalPrice < 0) {
                    totalPrice = 0;
                }
            }
        }

        // 4. 扣减库存（乐观锁）
        for (OrderItem item : orderItems) {
            LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Product::getId, item.getProductId())
                        .ge(Product::getStock, item.getQuantity())
                        .setSql("stock = stock - " + item.getQuantity())
                        .setSql("sales = sales + " + item.getQuantity());
            
            int updated = productMapper.update(null, updateWrapper);
            if (updated == 0) {
                throw new BusinessException(400, "商品 " + item.getProductName() + " 库存不足");
            }
        }

        // 5. 创建订单
        Order order = new Order();
        order.setId(redisIdWorker.nextId("order"));
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalPrice(totalPrice + discount);
        order.setActualValue(totalPrice + discount); // 商品原价
        order.setCouponId(couponId);
        order.setDiscount(discount);
        order.setPayValue(totalPrice);
        order.setStatus(1); // 待支付
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 6. 创建订单明细
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 7. 删除购物车中已结算的商品
        redisTemplate.opsForHash().delete(cartKey, 
            productIds.stream().map(String::valueOf).toArray());

        return order;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return timestamp + random;
    }
}
