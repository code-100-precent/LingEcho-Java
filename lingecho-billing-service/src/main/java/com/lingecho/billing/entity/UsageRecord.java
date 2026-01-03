package com.lingecho.billing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lingecho.common.core.BaseEntity;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * UsageRecord 实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("usage_records")
@EqualsAndHashCode(callSuper = true)
public class UsageRecord extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long groupId;

    private Long credentialId;

    private Long assistantId;

    private String sessionId;

    private Long callLogId;

    private String usageType; // llm, call, asr, tts, storage, api

    private String model;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Integer callDuration;

    private Integer callCount;

    private Integer audioDuration;

    private Long audioSize;

    private Long storageSize;

    private Integer apiCallCount;

    private String metadata;

    private String description;

    private LocalDateTime usageTime;
}

