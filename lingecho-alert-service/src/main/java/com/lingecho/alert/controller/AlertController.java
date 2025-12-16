package com.lingecho.alert.controller;

import com.lingecho.common.core.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 告警控制器
 */
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    @GetMapping
    public Result<List<AlertDTO>> listAlerts() {
        // TODO: 实现告警列表查询
        List<AlertDTO> alerts = new ArrayList<>();
        return Result.success(alerts);
    }

    @PostMapping
    public Result<AlertDTO> createAlert(@RequestBody CreateAlertRequest request) {
        // TODO: 实现告警创建
        AlertDTO alert = new AlertDTO();
        return Result.success(alert);
    }

    @PutMapping("/{id}")
    public Result<AlertDTO> updateAlert(@PathVariable Long id, @RequestBody UpdateAlertRequest request) {
        // TODO: 实现告警更新
        AlertDTO alert = new AlertDTO();
        return Result.success(alert);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAlert(@PathVariable Long id) {
        // TODO: 实现告警删除
        return Result.success();
    }

    @Data
    static class AlertDTO {
        private Long id;
        private String name;
        private String rule;
        private String status;
    }

    @Data
    static class CreateAlertRequest {
        private String name;
        private String rule;
    }

    @Data
    static class UpdateAlertRequest {
        private String name;
        private String rule;
        private String status;
    }
}

