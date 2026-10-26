package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.common.ErrorCode;
import com.campus.common.PageResult;
import com.campus.dto.NearbyShop;
import com.campus.entity.Shop;
import com.campus.entity.ShopCategory;
import com.campus.mapper.ShopMapper;
import com.campus.service.ShopCategoryService;
import com.campus.service.ShopService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商铺服务实现
 * 
 * 缓存策略：
 * - 缓存穿透防护：查询不存在的商铺时缓存空值（2分钟过期）
 * - 缓存击穿防护：使用 Redis SETNX 互斥锁，防止大量并发请求同时查询数据库
 * - 缓存雪崩防护：TTL = 30分钟 + 随机1~5分钟，避免大量缓存同时过期
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ShopCategoryService shopCategoryService;

    /** 缓存Key前缀 */
    private static final String CACHE_KEY_PREFIX = "shop:detail:";
    /** 互斥锁Key前缀 */
    private static final String LOCK_KEY_PREFIX = "shop:lock:";
    /** Redis GEO Key */
    private static final String GEO_KEY = "shop:geo";
    /** 缓存基础过期时间（秒） */
    private static final long CACHE_BASE_TTL_SECONDS = 30 * 60; // 30分钟
    /** 缓存随机偏移范围（秒）：1~5分钟 */
    private static final int CACHE_RANDOM_TTL_MIN = 60;
    private static final int CACHE_RANDOM_TTL_MAX = 300;
    /** 空值缓存过期时间（秒） */
    private static final long EMPTY_CACHE_TTL_SECONDS = 120; // 2分钟
    /** 互斥锁过期时间（秒） */
    private static final long LOCK_TTL_SECONDS = 10;
    /** 获取锁失败后的重试等待时间（毫秒） */
    private static final long LOCK_RETRY_SLEEP_MS = 50;
    /** 最大重试次数 */
    private static final int MAX_RETRY_COUNT = 10;

    /**
     * 应用启动时初始化 Redis GEO 数据
     * 把所有有经纬度的商铺位置加载到 Redis GEO 中
     */
    @PostConstruct
    public void initShopGeo() {
        try {
            // 检查 Redis 中是否已有数据
            Long size = redisTemplate.opsForZSet().size(GEO_KEY);
            if (size != null && size > 0) {
                log.info("Redis GEO 已有 {} 个商铺，跳过初始化", size);
                return;
            }

            // 从数据库加载所有有经纬度的商铺
            LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNotNull(Shop::getLongitude)
                   .isNotNull(Shop::getLatitude);
            List<Shop> shops = this.list(wrapper);

            if (shops.isEmpty()) {
                log.warn("没有找到有经纬度的商铺，跳过 GEO 初始化");
                return;
            }

            // 批量添加到 Redis GEO
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>();
            for (Shop shop : shops) {
                Point point = new Point(shop.getLongitude(), shop.getLatitude());
                locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(), point));
            }

            Long added = redisTemplate.opsForGeo().add(GEO_KEY, locations);
            log.info("Redis GEO 初始化完成，添加了 {} 个商铺位置", added);
        } catch (Exception e) {
            log.error("Redis GEO 初始化失败", e);
        }
    }

    @Override
    public PageResult<Shop> listForUser(int page, int size, Long categoryId, String name) {
        Page<Shop> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1) // 只查营业中的
                .eq(categoryId != null, Shop::getCategoryId, categoryId)
                .like(StringUtils.hasText(name), Shop::getName, name)
                .orderByDesc(Shop::getSold);

        Page<Shop> result = this.page(pageParam, wrapper);
        fillCategoryNames(result.getRecords());

        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }

    @Override
    public Shop getByIdWithCache(Long id) {
        return queryWithPassThroughAndBreakthrough(id);
    }

    /**
     * 核心方法：缓存穿透 + 缓存击穿 + 缓存雪崩 三重防护
     *
     * 流程：
     * 1. 查Redis缓存
     *    - 命中空值 → 返回null（穿透防护）
     *    - 命中数据 → 返回Shop对象
     * 2. 缓存未命中 → 尝试获取互斥锁
     *    - 获取锁成功 → 查数据库 → 写缓存 → 返回
     *    - 获取锁失败 → 等待后重试（击穿防护）
     */
    private Shop queryWithPassThroughAndBreakthrough(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        String lockKey = LOCK_KEY_PREFIX + id;
        //最多重试十次
        for (int retry = 0; retry < MAX_RETRY_COUNT; retry++) {
            // ① 查Redis缓存
            String cached = redisTemplate.opsForValue().get(cacheKey);

            // ② 缓存命中
            if (cached != null) {
                // 命中空值 → 说明这个商铺不存在（穿透防护）
                if (cached.isEmpty()) {
                    log.debug("商铺[{}] 命中空值缓存，商铺不存在", id);
                    return null;
                }
                // 命中数据 → 反序列化返回
                try {
                    log.debug("商铺[{}] 命中缓存", id);
                    return objectMapper.readValue(cached, Shop.class);
                } catch (JsonProcessingException e) {
                    log.warn("商铺[{}] 缓存反序列化失败，将重新查询", id);
                    redisTemplate.delete(cacheKey);
                }
            }

            // ③ 缓存未命中 → 尝试获取互斥锁（击穿防护） 获取互斥锁
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(locked)) {
                try {
                    // 双重检查：获取锁后再查一次缓存（可能别人已经写入了）
                    String doubleCheck = redisTemplate.opsForValue().get(cacheKey);
                    if (doubleCheck != null) {
                        if (doubleCheck.isEmpty()) return null;
                        try {
                            return objectMapper.readValue(doubleCheck, Shop.class);
                        } catch (JsonProcessingException e) {
                            log.warn("双重检查反序列化失败", e);
                        }
                    }

                    // ④ 查数据库
                    log.info("商铺[{}] 缓存未命中，查询数据库", id);
                    Shop shop = this.getById(id);

                    if (shop != null) {
                        // 查到了 → 写入缓存（雪崩防护：随机TTL）
                        long ttl = CACHE_BASE_TTL_SECONDS + ThreadLocalRandom.current()
                                .nextLong(CACHE_RANDOM_TTL_MIN, CACHE_RANDOM_TTL_MAX);
                        String json = objectMapper.writeValueAsString(shop);
                        redisTemplate.opsForValue().set(cacheKey, json, ttl, TimeUnit.SECONDS);
                        log.info("商铺[{}] 已写入缓存，TTL={}秒", id, ttl);
                        return shop;
                    } else {
                        // 查不到 → 缓存空值（穿透防护）
                        redisTemplate.opsForValue().set(cacheKey, "", EMPTY_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
                        log.info("商铺[{}] 不存在，已缓存空值，TTL={}秒", id, EMPTY_CACHE_TTL_SECONDS);
                        return null;
                    }
                } catch (JsonProcessingException e) {
                    log.error("商铺[{}] 序列化失败", id, e);
                    throw new BusinessException(ErrorCode.INTERNAL_ERROR, "数据序列化失败");
                } finally {
                    // ⑤ 释放锁
                    redisTemplate.delete(lockKey);
                }
            }

            // ⑥ 获取锁失败 → 短暂等待后重试
            log.debug("商铺[{}] 获取锁失败，等待{}ms后重试（第{}次）", id, LOCK_RETRY_SLEEP_MS, retry + 1);
            try {
                Thread.sleep(LOCK_RETRY_SLEEP_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "查询被中断");
            }
        }

        // 重试次数耗尽 → 降级直接查数据库
        log.warn("商铺[{}] 缓存获取锁重试{}次仍失败，降级查数据库", id, MAX_RETRY_COUNT);
        return this.getById(id);
    }

    @Override
    public List<NearbyShop> getNearbyShops(Double longitude, Double latitude, Double radius) {
        // 1. 使用 Redis GEO 查询附近商铺
        Point center = new Point(longitude, latitude);
        Distance distance = new Distance(radius, RedisGeoCommands.DistanceUnit.METERS);
        Circle circle = new Circle(center, distance);
        GeoReference<String> geoReference = GeoReference.fromCircle(circle);
        
        // Spring Data Redis 3.x API: search(key, reference, distance)
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .search(GEO_KEY, geoReference, distance);

        if (results == null || results.getContent().isEmpty()) {
            log.info("附近 {}m 内没有商铺", radius);
            return new ArrayList<>();
        }

        // 2. 提取商铺ID和距离，按距离排序
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> resultList = new ArrayList<>(results.getContent());
        resultList.sort(Comparator.comparingDouble(r -> r.getDistance().getValue()));
        
        // 限制返回数量为20个
        if (resultList.size() > 20) {
            resultList = resultList.subList(0, 20);
        }
        
        List<Long> shopIds = new ArrayList<>();
        Map<Long, Double> distanceMap = new HashMap<>();

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : resultList) {
            Long shopId = Long.parseLong(result.getContent().getName());
            shopIds.add(shopId);
            distanceMap.put(shopId, result.getDistance().getValue());
        }

        // 3. 批量查询商铺详情
        List<Shop> shops = this.listByIds(shopIds);
        
        // 4. 构建 NearbyShop DTO 列表
        List<NearbyShop> nearbyShops = new ArrayList<>();
        for (Shop shop : shops) {
            NearbyShop nearbyShop = new NearbyShop();
            nearbyShop.setShop(shop);
            nearbyShop.setDistance(distanceMap.get(shop.getId()));
            nearbyShops.add(nearbyShop);
        }

        // 5. 按距离排序
        nearbyShops.sort(Comparator.comparing(NearbyShop::getDistance));

        log.info("查询到 {} 个附近商铺", nearbyShops.size());
        return nearbyShops;
    }

    @Override
    public PageResult<Shop> pageForAdmin(int page, int size, String name, Long categoryId) {
        Page<Shop> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                .like(StringUtils.hasText(name), Shop::getName, name)
                .eq(categoryId != null, Shop::getCategoryId, categoryId)
                .orderByDesc(Shop::getId);

        Page<Shop> result = this.page(pageParam, wrapper);
        fillCategoryNames(result.getRecords());

        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Shop shop) {
        if (shop.getStatus() == null) shop.setStatus(1);
        if (shop.getSold() == null) shop.setSold(0);
        if (shop.getScore() == null) shop.setScore(0);
        if (shop.getAvgPrice() == null) shop.setAvgPrice(0);
        this.save(shop);
        log.info("新增商铺成功：{}", shop.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Shop shop) {
        this.updateById(shop);
        log.info("修改商铺成功：id={}", shop.getId());

        // 删除该商铺的缓存（下次查询时重新加载）
        deleteShopCache(shop.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Shop shop = this.getById(id);
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商铺不存在");
        }

        this.removeById(id);
        log.info("删除商铺成功：{}", shop.getName());

        // 删除该商铺的缓存
        deleteShopCache(id);
    }

    /**
     * 删除指定商铺的缓存
     */
    private void deleteShopCache(Long shopId) {
        String cacheKey = CACHE_KEY_PREFIX + shopId;
        try {
            Boolean result = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(result)) {
                log.debug("已删除商铺[{}]缓存", shopId);
            }
        } catch (Exception e) {
            log.error("删除商铺[{}]缓存失败", shopId, e);
        }
    }

    /**
     * 批量填充分类名称
     */
    private void fillCategoryNames(List<Shop> shops) {
        if (shops == null || shops.isEmpty()) return;

        Set<Long> categoryIds = shops.stream()
                .map(Shop::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) return;

        List<ShopCategory> categories = shopCategoryService.listByIds(categoryIds);
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(ShopCategory::getId, ShopCategory::getName, (a, b) -> a));

        for (Shop shop : shops) {
            if (shop.getCategoryId() != null) {
                shop.setCategoryName(categoryNameMap.getOrDefault(shop.getCategoryId(), ""));
            }
        }
    }
}
