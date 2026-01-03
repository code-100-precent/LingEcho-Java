package com.lingecho.knowledge.controller;

import com.lingecho.common.core.ApiResponse;
import com.lingecho.knowledge.entity.Knowledge;
import com.lingecho.knowledge.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 知识库控制器
 */
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 创建知识库
     */
    @PostMapping("/create")
    public ApiResponse<Knowledge> createKnowledge(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> request) {
        String knowledgeKey = (String) request.get("knowledgeKey");
        String knowledgeName = (String) request.get("knowledgeName");
        String provider = (String) request.get("provider");
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) request.get("config");
        Long groupId = request.get("groupId") != null ? Long.parseLong(request.get("groupId").toString()) : null;
        
        return knowledgeService.createKnowledge(userId, knowledgeKey, knowledgeName, provider, config, groupId);
    }

    /**
     * 获取用户的知识库列表
     */
    @GetMapping("/get")
    public ApiResponse<List<Knowledge>> getUserKnowledge(@RequestHeader("X-User-Id") Long userId) {
        return knowledgeService.getUserKnowledge(userId);
    }

    /**
     * 删除知识库
     */
    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteKnowledge(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long knowledgeId) {
        return knowledgeService.deleteKnowledge(userId, knowledgeId);
    }

    /**
     * 上传文件到知识库
     */
    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> uploadFileToKnowledgeBase(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long knowledgeId,
            @RequestParam("file") MultipartFile file) {
        try {
            byte[] fileContent = file.getBytes();
            return knowledgeService.uploadFileToKnowledgeBase(userId, knowledgeId, file.getOriginalFilename(), fileContent);
        } catch (Exception e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }
}

