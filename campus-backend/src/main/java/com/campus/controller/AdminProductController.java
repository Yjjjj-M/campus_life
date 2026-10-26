package com.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.Product;
import com.campus.entity.ProductCategory;
import com.campus.service.ProductCategoryService;
import com.campus.service.ProductService;
import com.campus.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端-商品控制器
 */
@Tag(name = "管理端-商品管理")
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class AdminProductController {
    
    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final ShopService shopService;
    
    @Operation(summary = "分页查询商品")
    @GetMapping("/page")
    public Result<PageResult<Product>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, Product::getShopId, shopId)
               .eq(categoryId != null, Product::getCategoryId, categoryId)
               .eq(status != null, Product::getStatus, status)
               .like(name != null && !name.isEmpty(), Product::getName, name)
               .orderByDesc(Product::getCreateTime);
        
        Page<Product> pageResult = productService.page(new Page<>(page, size), wrapper);
        
        // 填充分类名称和商铺名称
        List<Product> records = pageResult.getRecords();
        if (!records.isEmpty()) {
            List<Long> categoryIds = records.stream()
                    .map(Product::getCategoryId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Long> shopIds = records.stream()
                    .map(Product::getShopId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Long, String> categoryNameMap = categoryService.listByIds(categoryIds)
                    .stream()
                    .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
            Map<Long, String> shopNameMap = shopService.listByIds(shopIds)
                    .stream()
                    .collect(Collectors.toMap(s -> s.getId(), s -> s.getName()));
            
            records.forEach(p -> {
                p.setCategoryName(categoryNameMap.getOrDefault(p.getCategoryId(), "未知"));
                p.setShopName(shopNameMap.getOrDefault(p.getShopId(), "未知"));
            });
        }
        
        PageResult<Product> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(pageResult.getTotal());
        return Result.ok(result);
    }
    
    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }
    
    @Operation(summary = "新增商品")
    @PostMapping
    public Result<Void> add(@RequestBody Product product) {
        productService.save(product);
        return Result.ok();
    }
    
    @Operation(summary = "修改商品")
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.updateById(product);
        return Result.ok();
    }
    
    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.ok();
    }
    
    @Operation(summary = "商品上架")
    @PutMapping("/{id}/online")
    public Result<Void> online(@PathVariable Long id) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(1);
        productService.updateById(product);
        return Result.ok();
    }
    
    @Operation(summary = "商品下架")
    @PutMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(0);
        productService.updateById(product);
        return Result.ok();
    }
}
