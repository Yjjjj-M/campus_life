package com.campus.controller;

import com.campus.common.BusinessException;
import com.campus.common.ErrorCode;
import com.campus.common.Result;
import com.campus.entity.User;
import com.campus.mapper.UserMapper;
import com.campus.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/common")
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserMapper userMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        // 检查手机号是否已注册
        User existingUser = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, request.getPhone())
        );
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "手机号已注册");
        }

        // 创建新用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname("用户" + request.getPhone().substring(7));
        user.setRole(0);
        userMapper.insert(user);

        return Result.ok();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getPhone, request.getPhone())
        );
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "手机号或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "手机号或密码错误");
        }

        // 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("role", user.getRole());
        data.put("nickname", user.getNickname());

        return Result.ok(data);
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @NotBlank(message = "密码不能为空")
        @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20位之间")
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;

        @NotBlank(message = "密码不能为空")
        private String password;
    }
}
