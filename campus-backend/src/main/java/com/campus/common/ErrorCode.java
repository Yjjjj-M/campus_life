package com.campus.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误
    SUCCESS(0, "success"),
    PARAM_ERROR(40001, "参数校验失败"),

    // 认证错误
    UNAUTHORIZED(40101, "未登录"),
    TOKEN_EXPIRED(40102, "Token已过期"),

    // 权限错误
    FORBIDDEN(40301, "无权限"),

    // 资源错误
    NOT_FOUND(40401, "资源不存在"),

    // 业务冲突
    CONFLICT(40901, "业务冲突"),
    STOCK_NOT_ENOUGH(40902, "库存不足"),
    DUPLICATE_PURCHASE(40903, "不能重复购买"),
    ORDER_STATUS_ERROR(40904, "订单状态异常"),

    // 服务错误
    INTERNAL_ERROR(50001, "服务内部错误");

    private final int code;
    private final String message;
}
