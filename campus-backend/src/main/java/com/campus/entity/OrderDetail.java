package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 订单明细实体
 */
@Data
@TableName("order_detail")
public class OrderDetail {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 订单ID */
    private Long orderId;
    
    /** 优惠券标题 */
    private String voucherTitle;
    
    /** 优惠券副标题 */
    private String voucherSubTitle;
    
    /** 商铺名称 */
    private String shopName;
    
    /** 商铺地址 */
    private String shopAddress;
    
    /** 实付金额（分） */
    private Integer payValue;
    
    /** 抵扣金额（分） */
    private Integer actualValue;
    
    /** 使用规则 */
    private String rules;
}
