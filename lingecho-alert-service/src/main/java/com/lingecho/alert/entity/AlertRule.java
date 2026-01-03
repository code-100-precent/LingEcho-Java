package com.lingecho.alert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lingecho.common.core.BaseEntity;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AlertRule 实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("alert_rules")
@EqualsAndHashCode(callSuper = true)
public class AlertRule extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId; // 0表示系统级规则

    private String name;

    private String description;

    private String alertType; // system_error, quota_exceeded, service_error, custom

    private String severity; // critical, high, medium, low

    private String conditions; // JSON格式的触发条件

    private String channels; // JSON数组格式的通知渠道

    private String webhookUrl;

    private String webhookMethod;

    private Boolean enabled;

    private Integer cooldown; // 冷却时间（秒）

    private Long triggerCount;

    private LocalDateTime lastTriggerAt;
}

