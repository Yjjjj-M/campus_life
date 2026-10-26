package com.campus.controller.user;

import com.campus.common.Result;
import com.campus.interceptor.UserHolder;
import com.campus.dto.UserDTO;
import com.campus.entity.User;
import com.campus.service.FollowService;
import com.campus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/follow")
@RequiredArgsConstructor
public class FollowController {
    
    private final FollowService followService;
    private final UserService userService;
    
    /**
     * 关注用户
     */
    @PostMapping("/{followUserId}")
    public Result<Void> follow(@PathVariable Long followUserId) {
        Long userId = UserHolder.getUser();
        followService.follow(userId, followUserId);
        return Result.ok();
    }
    
    /**
     * 取消关注
     */
    @DeleteMapping("/{followUserId}")
    public Result<Void> unfollow(@PathVariable Long followUserId) {
        Long userId = UserHolder.getUser();
        followService.unfollow(userId, followUserId);
        return Result.ok();
    }
    
    /**
     * 查询我关注的人
     */
    @GetMapping("/following")
    public Result<List<UserDTO>> getFollowing() {
        Long userId = UserHolder.getUser();
        Set<Long> followingIds = followService.getFollowingList(userId);
        List<User> users = userService.listByIds(followingIds);
        List<UserDTO> result = users.stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setNickname(user.getNickname());
                    dto.setIcon(user.getIcon());
                    return dto;
                })
                .collect(Collectors.toList());
        return Result.ok(result);
    }
    
    /**
     * 查询我的粉丝
     */
    @GetMapping("/followers")
    public Result<List<UserDTO>> getFollowers() {
        Long userId = UserHolder.getUser();
        Set<Long> followerIds = followService.getFollowersList(userId);
        List<User> users = userService.listByIds(followerIds);
        List<UserDTO> result = users.stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setNickname(user.getNickname());
                    dto.setIcon(user.getIcon());
                    return dto;
                })
                .collect(Collectors.toList());
        return Result.ok(result);
    }
    
    /**
     * 查询是否关注了某用户
     */
    @GetMapping("/isFollowing/{followUserId}")
    public Result<Boolean> isFollowing(@PathVariable Long followUserId) {
        Long userId = UserHolder.getUser();
        boolean isFollowing = followService.isFollowed(userId, followUserId);
        return Result.ok(isFollowing);
    }
}
