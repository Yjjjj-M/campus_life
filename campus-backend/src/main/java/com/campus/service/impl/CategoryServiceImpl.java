package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.PageResult;
import com.campus.entity.ShopCategory;
import com.campus.mapper.ShopCategoryMapper;
import com.campus.service.ShopCategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商铺分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<ShopCategoryMapper, ShopCategory> implements ShopCategoryService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY = "shop_category:list";
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    public List<ShopCategory> listForUser() {
        // 1. 先查Redis缓存
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (StringUtils.hasText(cached)) {
                log.debug("分类列表命中缓存");
                return objectMapper.readValue(cached, new TypeReference<List<ShopCategory>>() {});
            }
        } catch (JsonProcessingException e) {
            log.warn("解析缓存数据失败，将重新查询数据库", e);
        }

        // 2. 缓存未命中，查数据库
        log.debug("分类列表未命中缓存，查询数据库");
        List<ShopCategory> list = this.list(
            new LambdaQueryWrapper<ShopCategory>()
                .eq(ShopCategory::getStatus, 1)
                .orderByAsc(ShopCategory::getSort)
        );

        // 3. 写入Redis缓存
        try {
            String json = objectMapper.writeValueAsString(list);
            redisTemplate.opsForValue().set(CACHE_KEY, json, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("分类列表已写入缓存，过期时间：{}分钟", CACHE_TTL_MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("序列化缓存数据失败", e);
        }

        return list;
    }

    @Override
    public PageResult<ShopCategory> pageForAdmin(int page, int size) {
        Page<ShopCategory> pageParam = new Page<>(page, size);
        Page<ShopCategory> result = this.page(pageParam,
            new LambdaQueryWrapper<ShopCategory>()
                .orderByAsc(ShopCategory::getSort)
        );

        PageResult<ShopCategory> pageResult = new PageResult<>();
        pageResult.setRecords(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setPage((int) result.getCurrent());
        pageResult.setSize((int) result.getSize());
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ShopCategory category) {
        // 检查名称是否重复
        long count = this.count(
            new LambdaQueryWrapper<ShopCategory>()
                .eq(ShopCategory::getName, category.getName())
        );
        if (count > 0) {
            throw new RuntimeException("分类名称已存在：" + category.getName());
        }

        // 设置默认值
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        this.save(category);
        log.info("新增分类成功：{}", category.getName());

        // 删除缓存，下次查询时重新加载
        deleteCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ShopCategory category) {
        // 检查名称是否重复（排除自己）
        long count = this.count(
            new LambdaQueryWrapper<ShopCategory>()
                .eq(ShopCategory::getName, category.getName())
                .ne(ShopCategory::getId, category.getId())
        );
        if (count > 0) {
            throw new RuntimeException("分类名称已存在：" + category.getName());
        }

        this.updateById(category);
        log.info("修改分类成功：{}", category.getName());

        // 删除缓存，下次查询时重新加载
        deleteCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ShopCategory category = this.getById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        this.removeById(id);
        log.info("删除分类成功：{}", category.getName());

        // 删除缓存，下次查询时重新加载
        deleteCache();
    }

    /**
     * 删除缓存
     */
    private void deleteCache() {
        try {
            Boolean result = redisTemplate.delete(CACHE_KEY);
            if (Boolean.TRUE.equals(result)) {
                log.debug("已删除分类缓存");
            }
        } catch (Exception e) {
            log.error("删除缓存失败", e);
            // 缓存删除失败不影响主流程，等待缓存过期后自动更新
        }
    }
}
