package com.lingecho.assistant.controller;

import com.lingecho.common.core.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 助手控制器
 */
@RestController
@RequestMapping("/assistants")
@RequiredArgsConstructor
public class AssistantController {

    @GetMapping
    public Result<List<AssistantDTO>> listAssistants() {
        // TODO: 实现助手列表查询
        List<AssistantDTO> assistants = new ArrayList<>();
        return Result.success(assistants);
    }

    @GetMapping("/{id}")
    public Result<AssistantDTO> getAssistant(@PathVariable Long id) {
        // TODO: 实现助手详情查询
        AssistantDTO assistant = new AssistantDTO();
        assistant.setId(id);
        return Result.success(assistant);
    }

    @PostMapping
    public Result<AssistantDTO> createAssistant(@RequestBody CreateAssistantRequest request) {
        // TODO: 实现助手创建
        AssistantDTO assistant = new AssistantDTO();
        return Result.success(assistant);
    }

    @PutMapping("/{id}")
    public Result<AssistantDTO> updateAssistant(@PathVariable Long id, @RequestBody UpdateAssistantRequest request) {
        // TODO: 实现助手更新
        AssistantDTO assistant = new AssistantDTO();
        return Result.success(assistant);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAssistant(@PathVariable Long id) {
        // TODO: 实现助手删除
        return Result.success();
    }

    @PostMapping("/{id}/chat")
    public Result<ChatResponse> chat(@PathVariable Long id, @RequestBody ChatRequest request) {
        // TODO: 实现聊天功能
        ChatResponse response = new ChatResponse();
        response.setMessage("This is a mock response");
        return Result.success(response);
    }

    @Data
    static class AssistantDTO {
        private Long id;
        private String name;
        private String description;
        private String model;
        private String systemPrompt;
    }

    @Data
    static class CreateAssistantRequest {
        private String name;
        private String description;
        private String model;
        private String systemPrompt;
    }

    @Data
    static class UpdateAssistantRequest {
        private String name;
        private String description;
        private String model;
        private String systemPrompt;
    }

    @Data
    static class ChatRequest {
        private String message;
        private String conversationId;
    }

    @Data
    static class ChatResponse {
        private String message;
        private String conversationId;
    }
}

