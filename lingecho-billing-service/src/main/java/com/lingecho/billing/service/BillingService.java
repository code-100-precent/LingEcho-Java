package com.lingecho.billing.service;

import com.lingecho.billing.entity.UsageRecord;
import com.lingecho.billing.mapper.UsageRecordMapper;
import com.lingecho.common.core.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计费服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final UsageRecordMapper usageRecordMapper;

    /**
     * 获取使用统计
     */
    public ApiResponse<Map<String, Object>> getUsageStatistics(
            Long userId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long credentialId,
            Long groupId) {
        
        // 默认时间范围：最近30天
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Map<String, Object>> statistics = usageRecordMapper.getUsageStatistics(userId, startTime, endTime);

        Map<String, Object> result = new HashMap<>();
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("statistics", statistics);

        return ApiResponse.success(result);
    }
}

