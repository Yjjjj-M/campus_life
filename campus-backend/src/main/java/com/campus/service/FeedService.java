package com.campus.service;

import com.campus.common.PageResult;
import com.campus.entity.Review;

public interface FeedService {
    
    /**
     * 推送评价到粉丝的 Feed 流
     */
    void pushReviewToFollowers(Long userId, Review review);
    
    /**
     * 查询我的 Feed 流（好友动态）
     */
    PageResult<Review> getMyFeed(Long userId, Long cursor, Integer size);
}
