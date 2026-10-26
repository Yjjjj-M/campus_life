package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.common.PageResult;
import com.campus.entity.Review;

public interface ReviewService extends IService<Review> {
    
    /**
     * 发布评价
     */
    Review publishReview(Long userId, Review review);
    
    /**
     * 查看商铺的评价列表（游标分页）
     */
    PageResult<Review> getReviewsByShop(Long shopId, Long cursor, Integer size, Long userId);
    
    /**
     * 点赞评价
     */
    void likeReview(Long userId, Long reviewId);
    
    /**
     * 取消点赞
     */
    void unlikeReview(Long userId, Long reviewId);
    
    /**
     * 查看我的评价列表
     */
    PageResult<Review> getMyReviews(Long userId, Integer page, Integer size);
    
    /**
     * 查看评价详情
     */
    Review getReviewDetail(Long reviewId, Long userId);
    
    /**
     * 删除评价（软删除）
     */
    void deleteReview(Long reviewId);
    
    /**
     * 查看所有评价（管理端）
     */
    PageResult<Review> getAllReviews(Integer page, Integer size, Long shopId);
    
    /**
     * 检查用户是否点赞了某个评价
     */
    Boolean isLiked(Long userId, Long reviewId);
}
