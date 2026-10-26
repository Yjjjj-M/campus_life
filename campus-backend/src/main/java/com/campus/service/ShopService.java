package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.common.PageResult;
import com.campus.dto.NearbyShop;
import com.campus.entity.Shop;

import java.util.List;

/**
 * 商铺服务接口
 */
public interface ShopService extends IService<Shop> {

    /**
     * 用户端：分页查询营业中的商铺
     *
     * @param page       页码
     * @param size       每页大小
     * @param categoryId 分类ID（可选）
     * @param name       商铺名称关键词（可选）
     * @return 分页结果
     */
    PageResult<Shop> listForUser(int page, int size, Long categoryId, String name);

    /**
     * 用户端：查询商铺详情（带缓存穿透+击穿防护）
     *
     * @param id 商铺ID
     * @return 商铺信息，不存在返回null
     */
    Shop getByIdWithCache(Long id);

    /**
     * 用户端：查询附近商铺
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @param radius    半径（米）
     * @return 附近商铺列表（按距离排序）
     */
    List<NearbyShop> getNearbyShops(Double longitude, Double latitude, Double radius);

    /**
     * 管理端：分页查询所有商铺
     *
     * @param page       页码
     * @param size       每页大小
     * @param name       商铺名称关键词（可选）
     * @param categoryId 分类ID（可选）
     * @return 分页结果
     */
    PageResult<Shop> pageForAdmin(int page, int size, String name, Long categoryId);

    /**
     * 管理端：新增商铺
     *
     * @param shop 商铺信息
     */
    void add(Shop shop);

    /**
     * 管理端：修改商铺
     *
     * @param shop 商铺信息
     */
    void update(Shop shop);

    /**
     * 管理端：删除商铺
     *
     * @param id 商铺ID
     */
    void delete(Long id);
}
