package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.common.PageResult;
import com.campus.entity.Follow;

import java.util.Set;

public interface FollowService extends IService<Follow> {
    
    /**
     * 关注用户
     */
    void follow(Long userId, Long followUserId);
    
    /**
     * 取消关注
     */
    void unfollow(Long userId, Long followUserId);
    
    /**
     * 查询是否已关注
     */
    boolean isFollowed(Long userId, Long followUserId);
    
    /**
     * 查询我关注的人
     */
    Set<Long> getFollowingList(Long userId);
    
    /**
     * 查询我的粉丝
     */
    Set<Long> getFollowersList(Long userId);
    
    /**
     * 查询关注数
     */
    long getFollowCount(Long userId);
    
    /**
     * 查询粉丝数
     */
    long getFollowerCount(Long userId);
}
