package com.lingecho.alert.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingecho.alert.entity.Alert;
import com.lingecho.alert.entity.AlertRule;
import com.lingecho.alert.mapper.AlertMapper;
import com.lingecho.alert.mapper.AlertRuleMapper;
import com.lingecho.common.core.ApiResponse;
import com.lingecho.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertMapper alertMapper;
    private final AlertRuleMapper alertRuleMapper;

    /**
     * 获取用户告警列表
     */
    public ApiResponse<List<Alert>> getUserAlerts(Long userId, String status) {
        LambdaQueryWrapper<Alert> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Alert::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Alert::getStatus, status);
        }
        queryWrapper.orderByDesc(Alert::getCreatedAt);
        List<Alert> alerts = alertMapper.selectList(queryWrapper);
        return ApiResponse.success(alerts);
    }

    /**
     * 创建告警规则
     */
    @Transactional
    public ApiResponse<AlertRule> createAlertRule(Long userId, AlertRule rule) {
        rule.setUserId(userId);
        rule.setEnabled(rule.getEnabled() != null ? rule.getEnabled() : true);
        rule.setCooldown(rule.getCooldown() != null ? rule.getCooldown() : 300);
        rule.setTriggerCount(0L);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        
        alertRuleMapper.insert(rule);
        return ApiResponse.success(rule);
    }

    /**
     * 获取告警规则列表
     */
    public ApiResponse<List<AlertRule>> getAlertRules(Long userId) {
        LambdaQueryWrapper<AlertRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlertRule::getUserId, userId)
                .or()
                .eq(AlertRule::getUserId, 0L) // 系统级规则
                .orderByDesc(AlertRule::getCreatedAt);
        List<AlertRule> rules = alertRuleMapper.selectList(queryWrapper);
        return ApiResponse.success(rules);
    }

    /**
     * 更新告警状态
     */
    @Transactional
    public ApiResponse<Alert> updateAlertStatus(Long userId, Long alertId, String status) {
        Alert alert = alertMapper.selectById(alertId);
        if (alert == null || !alert.getUserId().equals(userId)) {
            throw new BusinessException("告警不存在或无权操作");
        }

        alert.setStatus(status);
        if ("resolved".equals(status)) {
            alert.setResolvedAt(LocalDateTime.now());
            alert.setResolvedBy(userId);
        }
        alert.setUpdatedAt(LocalDateTime.now());
        alertMapper.updateById(alert);

        return ApiResponse.success(alert);
    }
}

