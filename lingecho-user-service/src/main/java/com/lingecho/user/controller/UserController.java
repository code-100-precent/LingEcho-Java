package com.lingecho.user.controller;

import com.lingecho.common.core.ApiResponse;
import com.lingecho.user.dto.UpdateUserRequest;
import com.lingecho.user.models.dto.RegisterUserForm;
import com.lingecho.user.models.entity.User;
import com.lingecho.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<Void> handleUserSignup(@RequestBody RegisterUserForm registerUserForm){
        return ApiResponse.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        return userService.getUserInfo(userId);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/me")
    public ApiResponse<User> updateCurrentUser(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpdateUserRequest request) {
        return userService.updateUser(userId, request);
    }

    /**
     * 更新用户偏好设置
     */
    @PutMapping("/me/preferences")
    public ApiResponse<User> updatePreferences(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> preferences) {
        return userService.updatePreferences(userId, preferences);
    }

    /**
     * 更新通知设置
     */
    @PutMapping("/me/notification-settings")
    public ApiResponse<User> updateNotificationSettings(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Boolean> settings) {
        return userService.updateNotificationSettings(userId, settings);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/me/stats")
    public ApiResponse<Map<String, Object>> getUserStats(@RequestHeader("X-User-Id") Long userId) {
        return userService.getUserStats(userId);
    }

    /**
     * 上传头像
     */
    @PostMapping("/me/avatar")
    public ApiResponse<User> uploadAvatar(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {
        String avatarUrl = request.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return ApiResponse.error("头像URL不能为空");
        }
        return userService.uploadAvatar(userId, avatarUrl);
    }
}

