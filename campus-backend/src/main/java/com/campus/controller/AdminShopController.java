package com.campus.controller;

import com.campus.common.BusinessException;
import com.campus.common.ErrorCode;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.Shop;
import com.campus.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-商铺控制器
 */
@RestController
@RequestMapping("/api/admin/shop")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    /**
     * 查询所有商铺（用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<Shop>> all() {
        List<Shop> list = shopService.list();
        return Result.ok(list);
    }

    /**
     * 分页查询所有商铺（含休息中的）
     */
    @GetMapping("/page")
    public Result<PageResult<Shop>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {
        PageResult<Shop> result = shopService.pageForAdmin(page, size, name, categoryId);
        return Result.ok(result);
    }

    /**
     * 新增商铺
     */
    @PostMapping
    public Result<Void> add(@Validated @RequestBody Shop shop) {
        shopService.add(shop);
        return Result.ok();
    }

    /**
     * 修改商铺
     */
    @PutMapping
    public Result<Void> update(@Validated @RequestBody Shop shop) {
        if (shop.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商铺ID不能为空");
        }
        shopService.update(shop);
        return Result.ok();
    }

    /**
     * 删除商铺
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shopService.delete(id);
        return Result.ok();
    }
}
