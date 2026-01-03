package com.lingecho.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingecho.common.core.ApiResponse;
import com.lingecho.common.core.exception.BusinessException;
import com.lingecho.user.models.entity.User;
import com.lingecho.user.mapper.UserMapper;
import com.lingecho.user.dto.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 获取用户信息
     */
    public ApiResponse<User> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !user.getEnabled()) {
            throw new BusinessException("用户不存在");
        }
        // 清除敏感信息
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public ApiResponse<User> updateUser(Long userId, UpdateUserRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null || !user.getEnabled()) {
            throw new BusinessException("用户不存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);

        if (request.getEmail() != null) {
            // 检查邮箱是否已被其他用户使用
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, request.getEmail().toLowerCase())
                    .ne(User::getId, userId);
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("该邮箱已被使用");
            }
            updateWrapper.set(User::getEmail, request.getEmail().toLowerCase());
        }

        if (request.getPhone() != null) {
            updateWrapper.set(User::getPhone, request.getPhone());
        }

        if (request.getFirstName() != null) {
            updateWrapper.set(User::getFirstName, request.getFirstName());
        }

        if (request.getLastName() != null) {
            updateWrapper.set(User::getLastName, request.getLastName());
        }

        if (request.getDisplayName() != null) {
            updateWrapper.set(User::getDisplayName, request.getDisplayName());
        }

        if (request.getLocale() != null) {
            updateWrapper.set(User::getLocale, request.getLocale());
        }

        if (request.getTimezone() != null) {
            updateWrapper.set(User::getTimezone, request.getTimezone());
        }

        if (request.getGender() != null) {
            updateWrapper.set(User::getGender, request.getGender());
        }

        if (request.getAvatar() != null) {
            updateWrapper.set(User::getAvatar, request.getAvatar());
        }

        updateWrapper.set(User::getUpdatedAt, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        // 重新查询用户
        User updatedUser = userMapper.selectById(userId);
        updatedUser.setPassword(null);
        return ApiResponse.success(updatedUser);
    }

    /**
     * 更新用户偏好设置
     */
    @Transactional
    public ApiResponse<User> updatePreferences(Long userId, Map<String, String> preferences) {
        User user = userMapper.selectById(userId);
        if (user == null || !user.getEnabled()) {
            throw new BusinessException("用户不存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);

        if (preferences.containsKey("timezone")) {
            updateWrapper.set(User::getTimezone, preferences.get("timezone"));
        }

        if (preferences.containsKey("locale")) {
            updateWrapper.set(User::getLocale, preferences.get("locale"));
        }

        updateWrapper.set(User::getUpdatedAt, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        User updatedUser = userMapper.selectById(userId);
        updatedUser.setPassword(null);
        return ApiResponse.success(updatedUser);
    }

    /**
     * 更新通知设置
     */
    @Transactional
    public ApiResponse<User> updateNotificationSettings(Long userId, Map<String, Boolean> settings) {
        User user = userMapper.selectById(userId);
        if (user == null || !user.getEnabled()) {
            throw new BusinessException("用户不存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);

        if (settings.containsKey("emailNotifications")) {
            updateWrapper.set(User::getEmailNotifications, settings.get("emailNotifications"));
        }

        if (settings.containsKey("pushNotifications")) {
            updateWrapper.set(User::getPushNotifications, settings.get("pushNotifications"));
        }

        if (settings.containsKey("systemNotifications")) {
            updateWrapper.set(User::getSystemNotifications, settings.get("systemNotifications"));
        }

        if (settings.containsKey("autoCleanUnreadEmails")) {
            updateWrapper.set(User::getAutoCleanUnreadEmails, settings.get("autoCleanUnreadEmails"));
        }

        updateWrapper.set(User::getUpdatedAt, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        User updatedUser = userMapper.selectById(userId);
        updatedUser.setPassword(null);
        return ApiResponse.success(updatedUser);
    }

    /**
     * 获取用户统计信息
     */
    public ApiResponse<Map<String, Object>> getUserStats(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !user.getEnabled()) {
            throw new BusinessException("用户不存在");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("loginCount", user.getLoginCount() != null ? user.getLoginCount() : 0);
        stats.put("lastLogin", user.getLastLogin());
        stats.put("emailVerified", user.getEmailVerified());
        stats.put("phoneVerified", user.getPhoneVerified());
        stats.put("twoFactorEnabled", user.getTwoFactorEnabled());

        return ApiResponse.success(stats);
    }

    /**
     * 上传头像
     */
    @Transactional
    public ApiResponse<User> uploadAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null || !user.getEnabled()) {
            throw new BusinessException("用户不存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getAvatar, avatarUrl)
                .set(User::getUpdatedAt, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        User updatedUser = userMapper.selectById(userId);
        updatedUser.setPassword(null);
        return ApiResponse.success(updatedUser);
    }
}

