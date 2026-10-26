package com.campus.common;

import lombok.Builder;
import lombok.Data;

/**
 * Dashboard 统计数据 DTO
 */
@Data
@Builder
public class DashboardStats {
    
    /** 商铺总数 */
    private Long shopCount;
    
    /** 用户总数 */
    private Long userCount;
    
    /** 订单总数 */
    private Long orderCount;
    
    /** 今日订单数 */
    private Long todayOrderCount;
    
    /** 今日销售额（分） */
    private Long todayRevenue;
}
