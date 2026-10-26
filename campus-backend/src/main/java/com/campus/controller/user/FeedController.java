package com.campus.controller.user;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.interceptor.UserHolder;
import com.campus.entity.Review;
import com.campus.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/feed")
@RequiredArgsConstructor
public class FeedController {
    
    private final FeedService feedService;
    
    /**
     * 查询我的 Feed 流（好友动态）
     */
    @GetMapping
    public Result<PageResult<Review>> getMyFeed(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserHolder.getUser();
        PageResult<Review> result = feedService.getMyFeed(userId, cursor, size);
        return Result.ok(result);
    }
}
