package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.Voucher;
import com.campus.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-优惠券管理")
@RestController
@RequestMapping("/api/admin/voucher")
@RequiredArgsConstructor
public class AdminVoucherController {
    
    private final VoucherService voucherService;
    
    @Operation(summary = "创建优惠券")
    @PostMapping
    public Result<Void> create(@RequestBody Voucher voucher) {
        voucherService.create(voucher);
        return Result.ok();
    }
    
    @Operation(summary = "修改优惠券")
    @PutMapping
    public Result<Void> update(@RequestBody Voucher voucher) {
        voucherService.update(voucher);
        return Result.ok();
    }
    
    @Operation(summary = "删除优惠券")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return Result.ok();
    }
    
    @Operation(summary = "分页查询优惠券")
    @GetMapping("/page")
    public Result<PageResult<Voucher>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        PageResult<Voucher> result = voucherService.pageForAdmin(page, size, shopId, type, status);
        return Result.ok(result);
    }
    
    @Operation(summary = "修改优惠券状态（上架/下架）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        voucherService.updateStatus(id, status);
        return Result.ok();
    }
}
