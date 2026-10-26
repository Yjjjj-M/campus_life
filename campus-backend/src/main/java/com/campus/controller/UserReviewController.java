package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.entity.Review;
import com.campus.interceptor.UserHolder;
import com.campus.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-商铺点评")
@RestController
@RequestMapping("/api/user/review")
@RequiredArgsConstructor
public class UserReviewController {
    
    private final ReviewService reviewService;
    
    @Operation(summary = "发布评价")
    @PostMapping
    public Result<Review> publishReview(@RequestBody Review review) {
        Long userId = UserHolder.getUserId();
        return Result.ok(reviewService.publishReview(userId, review));
    }
    
    @Operation(summary = "查看商铺的评价列表（游标分页）")
    @GetMapping("/shop/{shopId}")
    public Result<PageResult<Review>> getReviewsByShop(
            @PathVariable Long shopId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserHolder.getUserId();
        return Result.ok(reviewService.getReviewsByShop(shopId, cursor, size, userId));
    }
    
    @Operation(summary = "点赞评价")
    @PostMapping("/{reviewId}/like")
    public Result<Void> likeReview(@PathVariable Long reviewId) {
        Long userId = UserHolder.getUserId();
        reviewService.likeReview(userId, reviewId);
        return Result.ok();
    }
    
    @Operation(summary = "取消点赞")
    @DeleteMapping("/{reviewId}/like")
    public Result<Void> unlikeReview(@PathVariable Long reviewId) {
        Long userId = UserHolder.getUserId();
        reviewService.unlikeReview(userId, reviewId);
        return Result.ok();
    }
    
    @Operation(summary = "查看我的评价列表")
    @GetMapping("/my")
    public Result<PageResult<Review>> getMyReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserHolder.getUserId();
        return Result.ok(reviewService.getMyReviews(userId, page, size));
    }
    
    @Operation(summary = "查看评价详情")
    @GetMapping("/{reviewId}")
    public Result<Review> getReviewDetail(@PathVariable Long reviewId) {
        Long userId = UserHolder.getUserId();
        return Result.ok(reviewService.getReviewDetail(reviewId, userId));
    }
}
