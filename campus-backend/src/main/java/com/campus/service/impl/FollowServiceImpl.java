package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.common.ErrorCode;
import com.campus.entity.Follow;
import com.campus.mapper.FollowMapper;
import com.campus.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String FOLLOW_KEY_PREFIX = "follow:";
    private static final String FOLLOWER_KEY_PREFIX = "follower:";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long userId, Long followUserId) {
        // 1. 不能关注自己
        if (userId.equals(followUserId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能关注自己");
        }
        
        // 2. 检查是否已关注
        if (isFollowed(userId, followUserId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "已经关注该用户");
        }
        
        // 3. 保存关注关系到数据库
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowUserId(followUserId);
        this.save(follow);
        
        // 4. 更新 Redis 关注关系
        String followKey = FOLLOW_KEY_PREFIX + userId;
        String followerKey = FOLLOWER_KEY_PREFIX + followUserId;
        redisTemplate.opsForSet().add(followKey, followUserId.toString());
        redisTemplate.opsForSet().add(followerKey, userId.toString());
        
        log.info("用户 {} 关注了用户 {}", userId, followUserId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long userId, Long followUserId) {
        // 1. 检查是否已关注
        if (!isFollowed(userId, followUserId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "还未关注该用户");
        }
        
        // 2. 删除数据库中的关注关系
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
               .eq(Follow::getFollowUserId, followUserId);
        this.remove(wrapper);
        
        // 3. 删除 Redis 中的关注关系
        String followKey = FOLLOW_KEY_PREFIX + userId;
        String followerKey = FOLLOWER_KEY_PREFIX + followUserId;
        redisTemplate.opsForSet().remove(followKey, followUserId.toString());
        redisTemplate.opsForSet().remove(followerKey, userId.toString());
        
        log.info("用户 {} 取消关注用户 {}", userId, followUserId);
    }
    
    @Override
    public boolean isFollowed(Long userId, Long followUserId) {
        // 先查 Redis
        String followKey = FOLLOW_KEY_PREFIX + userId;
        Boolean hasFollowed = redisTemplate.opsForSet().isMember(followKey, followUserId.toString());
        if (Boolean.TRUE.equals(hasFollowed)) {
            return true;
        }
        
        // Redis 没有，查数据库
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
               .eq(Follow::getFollowUserId, followUserId);
        return this.count(wrapper) > 0;
    }
    
    @Override
    public Set<Long> getFollowingList(Long userId) {
        String followKey = FOLLOW_KEY_PREFIX + userId;
        Set<String> followingIds = redisTemplate.opsForSet().members(followKey);
        if (followingIds == null || followingIds.isEmpty()) {
            return Set.of();
        }
        return followingIds.stream().map(Long::parseLong).collect(Collectors.toSet());
    }
    
    @Override
    public Set<Long> getFollowersList(Long userId) {
        String followerKey = FOLLOWER_KEY_PREFIX + userId;
        Set<String> followerIds = redisTemplate.opsForSet().members(followerKey);
        if (followerIds == null || followerIds.isEmpty()) {
            return Set.of();
        }
        return followerIds.stream().map(Long::parseLong).collect(Collectors.toSet());
    }
    
    @Override
    public long getFollowCount(Long userId) {
        String followKey = FOLLOW_KEY_PREFIX + userId;
        Long count = redisTemplate.opsForSet().size(followKey);
        return count != null ? count : 0;
    }
    
    @Override
    public long getFollowerCount(Long userId) {
        String followerKey = FOLLOWER_KEY_PREFIX + userId;
        Long count = redisTemplate.opsForSet().size(followerKey);
        return count != null ? count : 0;
    }
}
