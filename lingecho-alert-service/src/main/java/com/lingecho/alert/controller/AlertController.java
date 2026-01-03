package com.lingecho.alert.controller;

import com.lingecho.alert.entity.Alert;
import com.lingecho.alert.entity.AlertRule;
import com.lingecho.alert.service.AlertService;
import com.lingecho.common.core.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 告警控制器
 */
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * 获取用户告警列表
     */
    @GetMapping
    public ApiResponse<List<Alert>> listAlerts(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String status) {
        return alertService.getUserAlerts(userId, status);
    }

    /**
     * 更新告警状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Alert> updateAlertStatus(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestParam String status) {
        return alertService.updateAlertStatus(userId, id, status);
    }

    /**
     * 创建告警规则
     */
    @PostMapping("/rules")
    public ApiResponse<AlertRule> createAlertRule(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AlertRule rule) {
        return alertService.createAlertRule(userId, rule);
    }

    /**
     * 获取告警规则列表
     */
    @GetMapping("/rules")
    public ApiResponse<List<AlertRule>> getAlertRules(@RequestHeader("X-User-Id") Long userId) {
        return alertService.getAlertRules(userId);
    }
}

