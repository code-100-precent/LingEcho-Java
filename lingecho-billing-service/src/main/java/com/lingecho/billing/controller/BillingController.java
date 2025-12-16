package com.lingecho.billing.controller;

import com.lingecho.common.core.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 计费控制器
 */
@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    @GetMapping("/usage")
    public Result<List<UsageDTO>> getUsage(@RequestParam(required = false) Long userId) {
        // TODO: 实现使用量查询
        List<UsageDTO> usage = new ArrayList<>();
        return Result.success(usage);
    }

    @GetMapping("/bills")
    public Result<List<BillDTO>> getBills(@RequestParam(required = false) Long userId) {
        // TODO: 实现账单查询
        List<BillDTO> bills = new ArrayList<>();
        return Result.success(bills);
    }

    @GetMapping("/quotas")
    public Result<List<QuotaDTO>> getQuotas(@RequestParam(required = false) Long userId) {
        // TODO: 实现配额查询
        List<QuotaDTO> quotas = new ArrayList<>();
        return Result.success(quotas);
    }

    @Data
    static class UsageDTO {
        private String service;
        private Long count;
        private Long amount;
    }

    @Data
    static class BillDTO {
        private Long id;
        private Long userId;
        private String period;
        private Double amount;
        private String status;
    }

    @Data
    static class QuotaDTO {
        private String service;
        private Long used;
        private Long total;
    }
}

