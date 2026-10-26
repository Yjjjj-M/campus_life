package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {
    
    /**
     * 扣减库存（乐观锁：库存 > 0 才允许扣减）
     * @param voucherId 券ID
     * @return 影响行数（0 表示库存不足）
     */
    @Update("UPDATE seckill_voucher SET stock = stock - 1 WHERE voucher_id = #{voucherId} AND stock > 0")
    int deductStock(@Param("voucherId") Long voucherId);
    
    /**
     * 恢复库存
     * @param voucherId 券ID
     */
    @Update("UPDATE seckill_voucher SET stock = stock + 1 WHERE voucher_id = #{voucherId}")
    int restoreStock(@Param("voucherId") Long voucherId);
}
