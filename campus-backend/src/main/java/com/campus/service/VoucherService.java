package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.common.PageResult;
import com.campus.entity.Voucher;

import java.util.List;

public interface VoucherService extends IService<Voucher> {
    
    /**
     * 管理端：创建优惠券
     */
    void create(Voucher voucher);
    
    /**
     * 管理端：修改优惠券
     */
    void update(Voucher voucher);
    
    /**
     * 管理端：删除优惠券
     */
    void delete(Long id);
    
    /**
     * 管理端：分页查询
     */
    PageResult<Voucher> pageForAdmin(int page, int size, Long shopId, Integer type, Integer status);
    
    /**
     * 管理端：修改状态（上架/下架）
     */
    void updateStatus(Long id, Integer status);
    
    /**
     * 用户端：查询商铺的优惠券列表
     */
    List<Voucher> listByShop(Long shopId);
    
    /**
     * 用户端：查询优惠券详情
     */
    Voucher getByIdForUser(Long id);
    
    /**
     * 用户端：秒杀优惠券
     * @param voucherId 优惠券ID
     * @return 订单ID（用于前端轮询订单状态）
     */
    Long seckillVoucher(Long voucherId);
}
