package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商铺实体
 */
@Data
@TableName("shop")
public class Shop {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商铺名称 */
    private String name;

    /** 分类ID */
    private Long categoryId;

    /** 图片URL，逗号分隔 */
    private String images;

    /** 商圈/区域 */
    private String area;

    /** 详细地址 */
    private String address;

    /** 经度 */
    private Double longitude;

    /** 纬度 */
    private Double latitude;

    /** 人均价格（分） */
    private Integer avgPrice;

    /** 评分（1-50，显示时除以10） */
    private Integer score;

    /** 销量 */
    private Integer sold;

    /** 状态：1=营业, 0=休息 */
    private Integer status;

    /** 营业时间 */
    private String openHours;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 分类名称（非数据库字段，Service层填充） */
    @TableField(exist = false)
    private String categoryName;
}
