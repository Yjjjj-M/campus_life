package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀库存实体
 */
@Data
@TableName("seckill_voucher")
public class SeckillVoucher {
    
    /**
     * 关联的券ID（也是主键）
     */
    @TableId(type = IdType.INPUT)
    private Long voucherId;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 开始时间
     */
    private LocalDateTime beginTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
