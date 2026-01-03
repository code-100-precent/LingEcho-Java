package com.lingecho.assistant.dto;

import lombok.Data;

/**
 * 更新助手请求
 */
@Data
public class UpdateAssistantRequest {
    private String name;
    private String description;
    private String icon;
    private String systemPrompt;
    private String personaTag;
    private Float temperature;
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

