package com.campus.service;

import com.campus.common.DashboardStats;

/**
 * Dashboard 统计服务接口
 */
public interface DashboardService {
    
    /**
     * 获取统计数据
     * @return 统计数据
     */
    DashboardStats getStats();
}
