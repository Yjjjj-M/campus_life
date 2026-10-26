package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.common.PageResult;
import com.campus.entity.ShopCategory;

import java.util.List;

/**
 * 商铺分类服务接口
 */
public interface ShopCategoryService extends IService<ShopCategory> {

    /**
     * 用户端：查询所有启用的分类列表（带缓存）
     *
     * @return 分类列表
     */
    List<ShopCategory> listForUser();

    /**
     * 管理端：分页查询所有分类
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ShopCategory> pageForAdmin(int page, int size);

    /**
     * 管理端：新增分类
     *
     * @param category 分类信息
     */
    void add(ShopCategory category);

    /**
     * 管理端：修改分类
     *
     * @param category 分类信息
     */
    void update(ShopCategory category);

    /**
     * 管理端：删除分类
     *
     * @param id 分类ID
     */
    void delete(Long id);
}
