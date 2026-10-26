package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.ProductCategory;
import com.campus.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-商品分类控制器
 */
@Tag(name = "管理端-商品分类")
@RestController
@RequestMapping("/api/admin/product-category")
@RequiredArgsConstructor
public class AdminProductCategoryController {
    
    private final ProductCategoryService productCategoryService;
    
    @Operation(summary = "获取分类列表")
    @GetMapping("/list")
    public Result<List<ProductCategory>> list() {
        List<ProductCategory> list = productCategoryService.list();
        return Result.ok(list);
    }
    
    @Operation(summary = "新增分类")
    @PostMapping
    public Result<Void> add(@RequestBody ProductCategory category) {
        productCategoryService.save(category);
        return Result.ok();
    }
    
    @Operation(summary = "修改分类")
    @PutMapping
    public Result<Void> update(@RequestBody ProductCategory category) {
        productCategoryService.updateById(category);
        return Result.ok();
    }
    
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productCategoryService.removeById(id);
        return Result.ok();
    }
}
