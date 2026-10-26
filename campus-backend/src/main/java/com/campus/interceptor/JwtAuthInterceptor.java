package com.campus.interceptor;

import com.campus.common.BusinessException;
import com.campus.common.ErrorCode;
import com.campus.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public JwtAuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtils.parseToken(token);
        if (claims == null) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        Long userId = claims.get("userId", Long.class);
        Integer role = claims.get("role", Integer.class);

        // 管理端接口需要 role=1
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/api/admin") && (role == null || role != 1)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        UserHolder.setUserId(userId);
        UserHolder.setRole(role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.clear();
    }
}
