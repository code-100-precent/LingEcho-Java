package com.lingecho.device.controller;

import com.lingecho.common.core.ApiResponse;
import com.lingecho.device.entity.Device;
import com.lingecho.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备控制器
 */
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * 绑定设备
     */
    @PostMapping("/bind/{assistantId}/{deviceCode}")
    public ApiResponse<Device> bindDevice(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long assistantId,
            @PathVariable String deviceCode) {
        return deviceService.bindDevice(userId, assistantId, deviceCode);
    }

    /**
     * 获取用户设备列表
     */
    @GetMapping("/bind/{assistantId}")
    public ApiResponse<List<Device>> getUserDevices(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long assistantId) {
        return deviceService.getUserDevices(userId, assistantId);
    }

    /**
     * 解绑设备
     */
    @PostMapping("/unbind")
    public ApiResponse<Void> unbindDevice(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {
        String deviceId = request.get("deviceId");
        return deviceService.unbindDevice(userId, deviceId);
    }

    /**
     * 更新设备信息
     */
    @PutMapping("/update/{id}")
    public ApiResponse<Device> updateDevice(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String alias = request.get("alias");
        return deviceService.updateDevice(userId, id, alias);
    }
}

