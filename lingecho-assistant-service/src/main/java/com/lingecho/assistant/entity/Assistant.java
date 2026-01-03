package com.lingecho.assistant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lingecho.common.core.BaseEntity;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Assistant 实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("assistants")
@EqualsAndHashCode(callSuper = true)
public class Assistant extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long groupId;

    private String name;

    private String description;

    private String icon;

    private String systemPrompt;

    private String personaTag;

    private Float temperature;

    private String jsSourceId;

    private Integer maxTokens;

    private String language;

    private String speaker;

    private Integer voiceCloneId;

    private String knowledgeBaseId;

    private String ttsProvider;

    private String apiKey;

    private String apiSecret;

    private String llmModel;

    private Boolean enableGraphMemory;
}

