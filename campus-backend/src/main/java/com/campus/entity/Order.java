package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("orders")
public class Order {
    
    @TableId(type = IdType.INPUT)
    private Long id;
    
    /** 订单编号 */
    private String orderNo;
    
    /** 用户ID */
    private Long userId;
    
    /** 优惠券ID */
    private Long voucherId;
    
    /** 商铺ID */
    private Long shopId;
    
    /** 订单状态：1-待支付 2-已支付 3-已使用 4-已取消 5-已退款 */
    private Integer status;
    
    /** 实付金额（分） */
    private Integer payValue;
    
    /** 抵扣金额（分） */
    private Integer actualValue;
    
    /** 商品总价（分） */
    private Integer totalPrice;
    
    /** 使用的优惠券ID */
    private Long couponId;
    
    /** 优惠金额（分） */
    private Integer discount;
    
    /** 支付时间 */
    private LocalDateTime payTime;
    
    /** 使用时间 */
    private LocalDateTime useTime;
    
    /** 取消时间 */
    private LocalDateTime cancelTime;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 以下字段用于展示，不映射到数据库
    @TableField(exist = false)
    private String voucherTitle;
    
    @TableField(exist = false)
    private String shopName;
    
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String userPhone;
}
