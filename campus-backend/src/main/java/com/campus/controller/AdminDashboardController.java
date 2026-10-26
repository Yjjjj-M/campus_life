package com.campus.controller;

import com.campus.common.DashboardStats;
import com.campus.common.Result;
import com.campus.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 Dashboard 控制器
 */
@Tag(name = "管理端-Dashboard")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    
    private final DashboardService dashboardService;
    
    @Operation(summary = "获取统计数据")
    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        return Result.ok(dashboardService.getStats());
    }
}
