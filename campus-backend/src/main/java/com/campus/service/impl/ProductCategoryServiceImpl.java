package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.ProductCategory;
import com.campus.mapper.ProductCategoryMapper;
import com.campus.service.ProductCategoryService;
import org.springframework.stereotype.Service;

/**
 * 商品分类 Service 实现
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> 
        implements ProductCategoryService {
}
