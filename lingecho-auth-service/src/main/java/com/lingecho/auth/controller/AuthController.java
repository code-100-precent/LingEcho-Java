package com.lingecho.auth.controller;

import com.lingecho.common.core.result.Result;
import com.lingecho.common.web.exception.BusinessException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        // TODO: 实现登录逻辑
        if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
            LoginResponse response = new LoginResponse();
            response.setToken("mock-jwt-token");
            response.setUsername(request.getUsername());
            return Result.success(response);
        }
        throw new BusinessException("用户名或密码错误");
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        // TODO: 实现注册逻辑
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        // TODO: 实现登出逻辑
        return Result.success();
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    static class RegisterRequest {
        private String username;
        private String password;
        private String email;
    }

    @Data
    static class LoginResponse {
        private String token;
        private String username;
    }
}

