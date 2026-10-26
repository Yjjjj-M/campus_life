package com.campus.dto;

import com.campus.entity.Shop;
import lombok.Data;

/**
 * 附近商铺信息（包含距离）
 */
@Data
public class NearbyShop {
    /**
     * 商铺信息
     */
    private Shop shop;
    
    /**
     * 距离（米）
     */
    private Double distance;
}
