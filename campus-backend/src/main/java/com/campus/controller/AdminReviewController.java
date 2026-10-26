package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.Review;
import com.campus.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-商铺点评")
@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {
    
    private final ReviewService reviewService;
    
    @Operation(summary = "查看所有评价列表")
    @GetMapping
    public Result<PageResult<Review>> getAllReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long shopId) {
        return Result.ok(reviewService.getAllReviews(page, size, shopId));
    }
    
    @Operation(summary = "删除评价（软删除）")
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return Result.ok();
    }
}
