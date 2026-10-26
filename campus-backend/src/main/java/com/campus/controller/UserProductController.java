package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.Result;
import com.campus.entity.Product;
import com.campus.entity.ProductCategory;
import com.campus.service.ProductCategoryService;
import com.campus.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户端-商品控制器
 */
@Tag(name = "用户端-商品")
@RestController
@RequestMapping("/api/user/product")
@RequiredArgsConstructor
public class UserProductController {
    
    private final ProductService productService;
    private final ProductCategoryService categoryService;
    
    @Operation(summary = "获取商铺的商品分类列表")
    @GetMapping("/shop/{shopId}/categories")
    public Result<List<ProductCategory>> getShopCategories(@PathVariable Long shopId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getShopId, shopId)
               .eq(Product::getStatus, 1)
               .select(Product::getCategoryId);
        List<Long> categoryIds = productService.list(wrapper)
                .stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        
        if (categoryIds.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        
        List<ProductCategory> categories = categoryService.listByIds(categoryIds);
        return Result.ok(categories);
    }
    
    @Operation(summary = "获取商铺的商品列表")
    @GetMapping("/shop/{shopId}/list")
    public Result<List<Product>> listByShop(
            @PathVariable Long shopId,
            @RequestParam(required = false) Long categoryId) {
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getShopId, shopId)
               .eq(Product::getStatus, 1)
               .eq(categoryId != null, Product::getCategoryId, categoryId)
               .orderByDesc(Product::getCreateTime);
        
        List<Product> list = productService.list(wrapper);
        return Result.ok(list);
    }
    
    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null || product.getStatus() != 1) {
            return Result.error(404, "商品不存在或已下架");
        }
        return Result.ok(product);
    }
}
