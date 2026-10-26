package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.ShopCategory;
import com.campus.service.ShopCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-商铺分类控制器
 */
@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final ShopCategoryService shopCategoryService;

    /**
     * 分页查询分类列表
     */
    @GetMapping("/page")
    public Result<PageResult<ShopCategory>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<ShopCategory> result = shopCategoryService.pageForAdmin(page, size);
        return Result.ok(result);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<Void> add(@Validated @RequestBody ShopCategory category) {
        shopCategoryService.add(category);
        return Result.ok();
    }

    /**
     * 修改分类
     */
    @PutMapping
    public Result<Void> update(@Validated @RequestBody ShopCategory category) {
        shopCategoryService.update(category);
        return Result.ok();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shopCategoryService.delete(id);
        return Result.ok();
    }
}
