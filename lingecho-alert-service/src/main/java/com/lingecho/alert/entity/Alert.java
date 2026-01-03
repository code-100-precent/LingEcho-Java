package com.lingecho.alert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lingecho.common.core.BaseEntity;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Alert 实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("alerts")
@EqualsAndHashCode(callSuper = true)
public class Alert extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long ruleId;

    private String alertType; // system_error, quota_exceeded, service_error, custom

    private String severity; // critical, high, medium, low

    private String title;

    private String message;

    private String data; // JSON格式的告警数据

    private String status; // active, resolved, muted

    private LocalDateTime resolvedAt;

    private Long resolvedBy;

    private Boolean notified;

    private LocalDateTime notifiedAt;
}

