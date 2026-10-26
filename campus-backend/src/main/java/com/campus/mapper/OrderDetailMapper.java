package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细Mapper
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
