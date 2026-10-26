package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long shopId;
    
    private Long orderId;
    
    private Integer rating;
    
    private String content;
    
    private String images;
    
    private Integer likeCount;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 以下字段不映射到数据库，用于展示
    @TableField(exist = false)
    private String userNickname;
    
    @TableField(exist = false)
    private String userAvatar;
    
    @TableField(exist = false)
    private String shopName;
    
    @TableField(exist = false)
    private Boolean hasLiked;
    
    @TableField(exist = false)
    private Boolean isFollowed;
}
