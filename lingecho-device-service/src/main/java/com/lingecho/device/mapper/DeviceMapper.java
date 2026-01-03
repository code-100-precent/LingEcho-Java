package com.lingecho.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingecho.device.entity.Device;
import org.apache.ibatis.annotations.Mapper;

/**
 * Device Mapper
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
}

