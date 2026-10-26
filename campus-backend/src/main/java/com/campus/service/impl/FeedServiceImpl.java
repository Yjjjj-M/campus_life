package com.campus.service.impl;

import com.campus.common.PageResult;
import com.campus.entity.Review;
import com.campus.entity.User;
import com.campus.service.FeedService;
import com.campus.service.FollowService;
import com.campus.service.ReviewService;
import com.campus.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    
    private final StringRedisTemplate redisTemplate;
    private final FollowService followService;
    private final ReviewService reviewService;
    private final UserService userService;
    
    private static final String FEED_KEY_PREFIX = "feed:user:";
    
    @Override
    public void pushReviewToFollowers(Long userId, Review review) {
        // 1. 查询所有粉丝
        Set<Long> followers = followService.getFollowersList(userId);
        
        if (followers.isEmpty()) {
            log.info("用户 {} 没有粉丝，跳过 Feed 推送", userId);
            return;
        }
        
        // 2. 计算时间戳（秒）
        long timestamp = review.getCreateTime().toEpochSecond(
            java.time.ZoneOffset.of("+08:00")
        );
        
        // 3. 推送到每个粉丝的收件箱
        for (Long followerId : followers) {
            String feedKey = FEED_KEY_PREFIX + followerId;
            redisTemplate.opsForZSet().add(
                feedKey,
                review.getId().toString(),
                timestamp
            );
        }
        
        log.info("用户 {} 的评价 {} 已推送到 {} 个粉丝的 Feed 流", 
                 userId, review.getId(), followers.size());
    }
    
    @Override
    public PageResult<Review> getMyFeed(Long userId, Long cursor, Integer size) {
        String feedKey = FEED_KEY_PREFIX + userId;
        
        // 1. 如果没有 cursor，使用当前时间戳
        if (cursor == null) {
            cursor = System.currentTimeMillis() / 1000 + 1;
        }
        
        // 2. 从 Redis ZSet 中取出评价 ID（游标分页）
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
            .reverseRangeByScoreWithScores(feedKey, 0, cursor, 0, size + 1);
        
        if (tuples == null || tuples.isEmpty()) {
            PageResult<Review> result = new PageResult<>();
            result.setRecords(Collections.emptyList());
            result.setTotal(0);
            result.setHasMore(false);
            result.setNextCursor(0L);
            return result;
        }
        
        // 3. 判断是否有下一页
        boolean hasMore = tuples.size() > size;
        List<ZSetOperations.TypedTuple<String>> tupleList = new ArrayList<>(tuples);
        if (hasMore) {
            tupleList = tupleList.subList(0, size);
        }
        
        // 4. 提取评价 ID
        List<Long> reviewIds = tupleList.stream()
            .map(tuple -> Long.parseLong(tuple.getValue()))
            .collect(Collectors.toList());
        
        // 5. 批量查询评价详情
        List<Review> reviews = reviewService.listByIds(reviewIds);
        
        // 6. 填充用户信息
        Set<Long> userIds = reviews.stream()
            .map(Review::getUserId)
            .collect(Collectors.toSet());
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getId, user -> user));
        
        for (Review review : reviews) {
            User user = userMap.get(review.getUserId());
            if (user != null) {
                review.setUserNickname(user.getNickname());
                review.setUserAvatar(user.getIcon());
            }
            // 检查当前用户是否点赞
            Boolean hasLiked = reviewService.isLiked(userId, review.getId());
            review.setHasLiked(hasLiked != null && hasLiked);
        }
        
        // 7. 计算下一页游标
        Long nextCursor = hasMore ? tupleList.get(tupleList.size() - 1).getScore().longValue() : 0L;
        
        // 8. 构建分页结果
        PageResult<Review> result = new PageResult<>();
        result.setRecords(reviews);
        result.setTotal(0);
        result.setHasMore(hasMore);
        result.setNextCursor(nextCursor);
        
        return result;
    }
}
