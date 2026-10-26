package com.campus.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("product")
@Schema(description = "商品")
public class Product {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "商铺ID")
    private Long shopId;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "商品名称")
    private String name;
    
    @Schema(description = "价格（分）")
    private Integer price;
    
    @Schema(description = "图片URL")
    private String image;
    
    @Schema(description = "商品描述")
    private String description;
    
    @Schema(description = "库存")
    private Integer stock;
    
    @Schema(description = "状态 1上架 0下架")
    private Integer status;
    
    @Schema(description = "销量")
    private Integer sales;
    
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "分类名称")
    private String categoryName;
    
    @TableField(exist = false)
    @Schema(description = "商铺名称")
    private String shopName;
}
