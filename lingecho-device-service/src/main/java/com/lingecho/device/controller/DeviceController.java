package com.lingecho.device.controller;

import com.lingecho.common.core.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备控制器
 */
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    @GetMapping
    public Result<List<DeviceDTO>> listDevices() {
        // TODO: 实现设备列表查询
        List<DeviceDTO> devices = new ArrayList<>();
        return Result.success(devices);
    }

    @GetMapping("/{id}")
    public Result<DeviceDTO> getDevice(@PathVariable Long id) {
        // TODO: 实现设备详情查询
        DeviceDTO device = new DeviceDTO();
        device.setId(id);
        return Result.success(device);
    }

    @PostMapping
    public Result<DeviceDTO> createDevice(@RequestBody CreateDeviceRequest request) {
        // TODO: 实现设备创建
        DeviceDTO device = new DeviceDTO();
        return Result.success(device);
    }

    @PutMapping("/{id}")
    public Result<DeviceDTO> updateDevice(@PathVariable Long id, @RequestBody UpdateDeviceRequest request) {
        // TODO: 实现设备更新
        DeviceDTO device = new DeviceDTO();
        return Result.success(device);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDevice(@PathVariable Long id) {
        // TODO: 实现设备删除
        return Result.success();
    }

    @Data
    static class DeviceDTO {
        private Long id;
        private String name;
        private String type;
        private String status;
        private String firmwareVersion;
    }

    @Data
    static class CreateDeviceRequest {
        private String name;
        private String type;
    }

    @Data
    static class UpdateDeviceRequest {
        private String name;
        private String status;
    }
}

