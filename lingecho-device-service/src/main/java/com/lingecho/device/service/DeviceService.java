package com.lingecho.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingecho.device.entity.Device;
import com.lingecho.device.mapper.DeviceMapper;
import com.lingecho.common.core.ApiResponse;
import com.lingecho.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceMapper deviceMapper;

    /**
     * 绑定设备
     */
    @Transactional
    public ApiResponse<Device> bindDevice(Long userId, Long assistantId, String deviceCode) {
        // 查找设备
        Device device = deviceMapper.selectById(deviceCode);
        if (device == null) {
            // 如果设备不存在，创建新设备
            device = Device.builder()
                    .id(Long.parseLong(deviceCode))
                    .userId(userId)
                    .assistantId(assistantId)
                    .macAddress(deviceCode)
                    .autoUpdate(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            deviceMapper.insert(device);
        } else {
            // 更新设备绑定信息
            LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Device::getId, device.getId())
                    .set(Device::getUserId, userId)
                    .set(Device::getAssistantId, assistantId)
                    .set(Device::getUpdatedAt, LocalDateTime.now());
            deviceMapper.update(null, updateWrapper);
        }

        return ApiResponse.success(device);
    }

    /**
     * 获取用户设备列表
     */
    public ApiResponse<List<Device>> getUserDevices(Long userId, Long assistantId) {
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Device::getUserId, userId);
        if (assistantId != null) {
            queryWrapper.eq(Device::getAssistantId, assistantId);
        }
        List<Device> devices = deviceMapper.selectList(queryWrapper);
        return ApiResponse.success(devices);
    }

    /**
     * 解绑设备
     */
    @Transactional
    public ApiResponse<Void> unbindDevice(Long userId, String deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null || !device.getUserId().equals(userId)) {
            throw new BusinessException("设备不存在或无权操作");
        }

        LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Device::getId, device.getId())
                .set(Device::getUserId, null)
                .set(Device::getAssistantId, null)
                .set(Device::getUpdatedAt, LocalDateTime.now());
        deviceMapper.update(null, updateWrapper);

        return ApiResponse.success();
    }

    /**
     * 更新设备信息
     */
    @Transactional
    public ApiResponse<Device> updateDevice(Long userId, String deviceId, String alias) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null || !device.getUserId().equals(userId)) {
            throw new BusinessException("设备不存在或无权操作");
        }

        LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Device::getId, device.getId())
                .set(Device::getAlias, alias)
                .set(Device::getUpdatedAt, LocalDateTime.now());
        deviceMapper.update(null, updateWrapper);

        Device updatedDevice = deviceMapper.selectById(deviceId);
        return ApiResponse.success(updatedDevice);
    }
}

