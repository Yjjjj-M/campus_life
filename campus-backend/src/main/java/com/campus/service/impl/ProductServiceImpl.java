package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.Product;
import com.campus.mapper.ProductMapper;
import com.campus.service.ProductService;
import org.springframework.stereotype.Service;

/**
 * 商品 Service 实现
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> 
        implements ProductService {
}
