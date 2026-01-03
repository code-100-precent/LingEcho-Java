package com.lingecho.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建助手请求
 */
@Data
public class CreateAssistantRequest {
    @NotBlank(message = "助手名称不能为空")
    private String name;

    private String description;

    private String icon;

    private Long groupId;
}

