package com.lingecho.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lingecho.common.core.BaseEntity;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Device 实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("devices")
@EqualsAndHashCode(callSuper = true)
public class Device extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long groupId;

    private String macAddress;

    private String board;

    private String appVersion;

    private Integer autoUpdate;

    private Long assistantId;

    private String alias;

    private LocalDateTime lastConnected;
}

