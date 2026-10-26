package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.Voucher;
import com.campus.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-优惠券查询")
@RestController
@RequestMapping("/api/user/voucher")
@RequiredArgsConstructor
public class UserVoucherController {
    
    private final VoucherService voucherService;
    
    @Operation(summary = "查询商铺的优惠券列表")
    @GetMapping("/list")
    public Result<List<Voucher>> listByShop(@RequestParam Long shopId) {
        List<Voucher> list = voucherService.listByShop(shopId);
        return Result.ok(list);
    }
    
    @Operation(summary = "查询优惠券详情")
    @GetMapping("/{id}")
    public Result<Voucher> detail(@PathVariable Long id) {
        Voucher voucher = voucherService.getByIdForUser(id);
        return Result.ok(voucher);
    }
    
    @Operation(summary = "秒杀优惠券")
    @PostMapping("/seckill/{id}")
    public Result<Long> seckill(@PathVariable Long id) {
        Long orderId = voucherService.seckillVoucher(id);
        return Result.ok(orderId);
    }
}
