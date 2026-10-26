package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.common.ErrorCode;
import com.campus.common.PageResult;
import com.campus.entity.Review;
import com.campus.entity.ReviewLike;
import com.campus.entity.Shop;
import com.campus.entity.User;
import com.campus.mapper.ReviewLikeMapper;
import com.campus.mapper.ReviewMapper;
import com.campus.service.FeedService;
import com.campus.service.FollowService;
import com.campus.service.ReviewService;
import com.campus.service.ShopService;
import com.campus.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
    
    private final ReviewLikeMapper reviewLikeMapper;
    private final UserService userService;
    private final ShopService shopService;
    private final StringRedisTemplate redisTemplate;
    private final FollowService followService;
    
    // 使用 @Lazy 打破循环依赖（ReviewServiceImpl <-> FeedServiceImpl）
    @Autowired
    @Lazy
    private FeedService feedService;
    
    private static final String REVIEW_LIKE_KEY = "review:like:";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Review publishReview(Long userId, Review review) {
        // 1. 参数校验
        if (review.getShopId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商铺ID不能为空");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评分必须在1-5之间");
        }
        
        // 2. 验证商铺存在
        Shop shop = shopService.getById(review.getShopId());
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商铺不存在");
        }
        
        // 3. 设置用户ID和初始值
        review.setUserId(userId);
        review.setLikeCount(0);
        review.setStatus(1);
        
        // 4. 保存到数据库
        this.save(review);
        
        // 5. 推送到粉丝的 Feed 流
        feedService.pushReviewToFollowers(userId, review);
        
        log.info("用户 {} 发布了评价 {}", userId, review.getId());
        return review;
    }
    
    @Override
    public PageResult<Review> getReviewsByShop(Long shopId, Long cursor, Integer size, Long userId) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getShopId, shopId)
               .eq(Review::getStatus, 1)
               .orderByDesc(Review::getCreateTime);
        
        // 2. 游标分页：如果有 cursor，查询 create_time < cursor 的记录
        if (cursor != null && cursor > 0) {
            wrapper.lt(Review::getId, cursor);
        }
        
        // 3. 多查一条，用于判断是否有下一页
        wrapper.last("LIMIT " + (size + 1));
        
        // 4. 执行查询
        List<Review> reviews = this.list(wrapper);
        
        // 5. 判断是否有下一页
        boolean hasMore = reviews.size() > size;
        if (hasMore) {
            reviews = reviews.subList(0, size);
        }
        
        // 6. 填充用户信息和点赞状态
        fillReviewInfo(reviews, userId);
        
        // 7. 构建分页结果
        PageResult<Review> result = new PageResult<>();
        result.setRecords(reviews);
        result.setTotal(0); // 游标分页不需要总数
        result.setHasMore(hasMore);
        
        // 8. 设置下一页游标
        if (!reviews.isEmpty()) {
            result.setNextCursor(reviews.get(reviews.size() - 1).getId());
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeReview(Long userId, Long reviewId) {
        // 1. 验证评价存在
        Review review = this.getById(reviewId);
        if (review == null || review.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价不存在");
        }
        
        // 2. 检查是否已经点赞（Redis 查询，高性能）
        String redisKey = REVIEW_LIKE_KEY + reviewId;
        Boolean hasLiked = redisTemplate.opsForSet().isMember(redisKey, userId.toString());
        if (Boolean.TRUE.equals(hasLiked)) {
            throw new BusinessException(ErrorCode.CONFLICT, "您已经点赞过");
        }
        
        // 3. 记录点赞（数据库层面去重，唯一索引保证）
        ReviewLike like = new ReviewLike();
        like.setUserId(userId);
        like.setReviewId(reviewId);
        reviewLikeMapper.insert(like);
        
        // 4. 更新 Redis 点赞记录
        redisTemplate.opsForSet().add(redisKey, userId.toString());
        
        // 5. 更新点赞数（数据库）
        this.update(new LambdaUpdateWrapper<Review>()
                .eq(Review::getId, reviewId)
                .setSql("like_count = like_count + 1"));
        
        log.info("用户 {} 点赞了评价 {}", userId, reviewId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeReview(Long userId, Long reviewId) {
        // 1. 检查是否已经点赞
        String redisKey = REVIEW_LIKE_KEY + reviewId;
        Boolean hasLiked = redisTemplate.opsForSet().isMember(redisKey, userId.toString());
        if (!Boolean.TRUE.equals(hasLiked)) {
            // 再检查数据库（Redis 可能还没加载）
            ReviewLike like = reviewLikeMapper.selectOne(new LambdaQueryWrapper<ReviewLike>()
                    .eq(ReviewLike::getUserId, userId)
                    .eq(ReviewLike::getReviewId, reviewId));
            if (like == null) {
                throw new BusinessException(ErrorCode.CONFLICT, "您还没有点赞");
            }
        }
        
        // 2. 删除点赞记录（数据库）
        reviewLikeMapper.delete(new LambdaQueryWrapper<ReviewLike>()
                .eq(ReviewLike::getUserId, userId)
                .eq(ReviewLike::getReviewId, reviewId));
        
        // 3. 删除 Redis 点赞记录
        redisTemplate.opsForSet().remove(redisKey, userId.toString());
        
        // 4. 更新点赞数（数据库）
        this.update(new LambdaUpdateWrapper<Review>()
                .eq(Review::getId, reviewId)
                .setSql("like_count = GREATEST(like_count - 1, 0)"));
        
        log.info("用户 {} 取消点赞评价 {}", userId, reviewId);
    }
    
    @Override
    public PageResult<Review> getMyReviews(Long userId, Integer page, Integer size) {
        // 1. 分页查询
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId)
               .eq(Review::getStatus, 1)
               .orderByDesc(Review::getCreateTime);
        
        Page<Review> result = this.page(pageParam, wrapper);
        
        // 2. 填充信息
        fillReviewInfo(result.getRecords(), userId);
        
        // 3. 构建分页结果
        PageResult<Review> pageResult = new PageResult<>();
        pageResult.setRecords(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(page);
        pageResult.setSize(size);
        
        return pageResult;
    }
    
    @Override
    public Review getReviewDetail(Long reviewId, Long userId) {
        Review review = this.getById(reviewId);
        if (review == null || review.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价不存在");
        }
        
        // 填充信息
        fillReviewInfo(List.of(review), userId);
        
        return review;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long reviewId) {
        Review review = this.getById(reviewId);
        if (review == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价不存在");
        }
        
        // 软删除：设置 status = 0
        review.setStatus(0);
        this.updateById(review);
        
        log.info("评价 {} 已被删除", reviewId);
    }
    
    @Override
    public PageResult<Review> getAllReviews(Integer page, Integer size, Long shopId) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        
        if (shopId != null) {
            wrapper.eq(Review::getShopId, shopId);
        }
        
        wrapper.eq(Review::getStatus, 1)
               .orderByDesc(Review::getCreateTime);
        
        Page<Review> result = this.page(pageParam, wrapper);
        
        // 填充信息（管理端不需要点赞状态）
        fillReviewInfo(result.getRecords(), null);
        
        PageResult<Review> pageResult = new PageResult<>();
        pageResult.setRecords(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(page);
        pageResult.setSize(size);
        
        return pageResult;
    }
    
    /**
     * 填充评价的额外信息（用户昵称、商铺名称、点赞状态、关注状态）
     */
    private void fillReviewInfo(List<Review> reviews, Long userId) {
        if (reviews == null || reviews.isEmpty()) {
            return;
        }
        
        // 1. 批量查询点赞状态（如果提供了 userId）
        if (userId != null) {
            for (Review review : reviews) {
                String redisKey = REVIEW_LIKE_KEY + review.getId();
                Boolean hasLiked = redisTemplate.opsForSet().isMember(redisKey, userId.toString());
                review.setHasLiked(Boolean.TRUE.equals(hasLiked));
            }
        }
        
        // 2. 批量查询用户信息
        Set<Long> userIds = new java.util.HashSet<>();
        for (Review review : reviews) {
            userIds.add(review.getUserId());
        }
        
        List<User> users = userService.listByIds(userIds);
        users.forEach(user -> {
            for (Review review : reviews) {
                if (review.getUserId().equals(user.getId())) {
                    review.setUserNickname(user.getNickname());
                    review.setUserAvatar(user.getIcon());
                }
            }
        });
        
        // 3. 批量查询商铺信息
        Set<Long> shopIds = new java.util.HashSet<>();
        for (Review review : reviews) {
            shopIds.add(review.getShopId());
        }
        
        List<Shop> shops = shopService.listByIds(shopIds);
        shops.forEach(shop -> {
            for (Review review : reviews) {
                if (review.getShopId().equals(shop.getId())) {
                    review.setShopName(shop.getName());
                }
            }
        });
        
        // 4. 填充关注状态（如果提供了 userId）
        if (userId != null) {
            for (Review review : reviews) {
                // 不能关注自己
                if (review.getUserId().equals(userId)) {
                    review.setIsFollowed(false);
                } else {
                    boolean isFollowed = followService.isFollowed(userId, review.getUserId());
                    review.setIsFollowed(isFollowed);
                }
            }
        }
    }
    
    @Override
    public Boolean isLiked(Long userId, Long reviewId) {
        if (userId == null || reviewId == null) {
            return false;
        }
        String redisKey = REVIEW_LIKE_KEY + reviewId;
        return redisTemplate.opsForSet().isMember(redisKey, userId.toString());
    }
}
