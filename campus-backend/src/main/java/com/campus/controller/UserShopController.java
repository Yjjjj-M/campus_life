package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.dto.NearbyShop;
import com.campus.entity.Shop;
import com.campus.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端-商铺控制器
 * 
 * 路径前缀 /api/user/shop 已在 WebMvcConfig 中排除认证，无需登录即可访问
 */
@RestController
@RequestMapping("/api/user/shop")
@RequiredArgsConstructor
public class UserShopController {

    private final ShopService shopService;

    /**
     * 分页查询营业中的商铺列表
     */
    @GetMapping("/list")
    public Result<PageResult<Shop>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name) {
        PageResult<Shop> result = shopService.listForUser(page, size, categoryId, name);
        return Result.ok(result);
    }

    /**
     * 查询商铺详情（带缓存穿透+击穿+雪崩防护）
     */
    @GetMapping("/{id}")
    public Result<Shop> detail(@PathVariable Long id) {
        Shop shop = shopService.getByIdWithCache(id);
        return Result.ok(shop);
    }

    /**
     * 查询附近商铺
     * 
     * @param longitude 经度
     * @param latitude  纬度
     * @param radius    半径（米），默认3000米
     * @return 附近商铺列表（按距离排序）
     */
    @GetMapping("/nearby")
    public Result<List<NearbyShop>> nearby(
            @RequestParam Double longitude,
            @RequestParam Double latitude,
            @RequestParam(defaultValue = "3000") Double radius) {
        List<NearbyShop> result = shopService.getNearbyShops(longitude, latitude, radius);
        return Result.ok(result);
    }
}
