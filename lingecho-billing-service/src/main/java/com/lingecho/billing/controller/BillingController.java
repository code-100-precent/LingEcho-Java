package com.lingecho.billing.controller;

import com.lingecho.billing.service.BillingService;
import com.lingecho.common.core.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 计费控制器
 */
@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    /**
     * 获取使用统计
     */
    @GetMapping("/usage/statistics")
    public ApiResponse<Map<String, Object>> getUsageStatistics(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long credentialId,
            @RequestParam(required = false) Long groupId) {
        return billingService.getUsageStatistics(userId, startTime, endTime, credentialId, groupId);
    }
}

