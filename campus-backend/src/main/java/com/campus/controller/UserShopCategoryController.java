package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.ShopCategory;
import com.campus.service.ShopCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端-商铺分类控制器
 */
@RestController
@RequestMapping("/api/user/shop/category")
@RequiredArgsConstructor
public class UserShopCategoryController {

    private final ShopCategoryService shopCategoryService;

    /**
     * 查询所有启用的分类列表（带缓存）
     */
    @GetMapping("/list")
    public Result<List<ShopCategory>> list() {
        List<ShopCategory> list = shopCategoryService.listForUser();
        return Result.ok(list);
    }
}
