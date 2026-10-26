package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("voucher")
public class Voucher {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long shopId;
    
    private String title;
    
    private String subTitle;
    
    private String rules;
    
    /**
     * 实付金额（分）
     */
    private Integer payValue;
    
    /**
     * 抵扣金额（分）
     */
    private Integer actualValue;
    
    /**
     * 类型：0=普通券, 1=秒杀券
     */
    private Integer type;
    
    /**
     * 状态：1=上架, 2=下架, 3=过期
     */
    private Integer status;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 开始时间（秒杀券专用）
     */
    private LocalDateTime beginTime;
    
    /**
     * 结束时间（秒杀券专用）
     */
    private LocalDateTime endTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
