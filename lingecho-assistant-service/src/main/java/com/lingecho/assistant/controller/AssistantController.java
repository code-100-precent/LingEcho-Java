package com.lingecho.assistant.controller;

import com.lingecho.assistant.dto.CreateAssistantRequest;
import com.lingecho.assistant.dto.UpdateAssistantRequest;
import com.lingecho.assistant.entity.Assistant;
import com.lingecho.assistant.service.AssistantService;
import com.lingecho.common.core.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 助手控制器
 */
@RestController
@RequestMapping("/assistants")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    /**
     * 获取助手列表
     */
    @GetMapping
    public ApiResponse<List<Assistant>> listAssistants(@RequestHeader("X-User-Id") Long userId) {
        return assistantService.listAssistants(userId);
    }

    /**
     * 获取助手详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Assistant> getAssistant(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return assistantService.getAssistant(id, userId);
    }

    /**
     * 创建助手
     */
    @PostMapping
    public ApiResponse<Assistant> createAssistant(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateAssistantRequest request) {
        return assistantService.createAssistant(userId, request);
    }

    /**
     * 更新助手
     */
    @PutMapping("/{id}")
    public ApiResponse<Assistant> updateAssistant(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpdateAssistantRequest request) {
        return assistantService.updateAssistant(id, userId, request);
    }

    /**
     * 删除助手
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAssistant(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return assistantService.deleteAssistant(id, userId);
    }
}

