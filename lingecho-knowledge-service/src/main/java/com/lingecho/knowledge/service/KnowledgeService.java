package com.lingecho.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingecho.knowledge.entity.Knowledge;
import com.lingecho.knowledge.mapper.KnowledgeMapper;
import com.lingecho.common.core.ApiResponse;
import com.lingecho.common.core.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建知识库
     */
    @Transactional
    public ApiResponse<Knowledge> createKnowledge(
            Long userId,
            String knowledgeKey,
            String knowledgeName,
            String provider,
            Map<String, Object> config,
            Long groupId) {
        
        // 检查知识库标识键是否已存在
        LambdaQueryWrapper<Knowledge> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Knowledge::getKnowledgeKey, knowledgeKey)
                .eq(Knowledge::getUserId, userId);
        if (knowledgeMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("该知识库标识键已存在");
        }

        // 默认provider为aliyun
        if (provider == null || provider.isEmpty()) {
            provider = "aliyun";
        }

        // 序列化配置信息
        String configJson = "";
        try {
            if (config != null) {
                configJson = objectMapper.writeValueAsString(config);
            }
        } catch (Exception e) {
            throw new BusinessException("配置信息序列化失败: " + e.getMessage());
        }

        Knowledge knowledge = Knowledge.builder()
                .userId(userId)
                .groupId(groupId)
                .knowledgeKey(knowledgeKey)
                .knowledgeName(knowledgeName)
                .provider(provider)
                .config(configJson)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        knowledgeMapper.insert(knowledge);
        return ApiResponse.success(knowledge);
    }

    /**
     * 获取用户的知识库列表
     */
    public ApiResponse<List<Knowledge>> getUserKnowledge(Long userId) {
        LambdaQueryWrapper<Knowledge> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Knowledge::getUserId, userId)
                .orderByDesc(Knowledge::getCreatedAt);
        List<Knowledge> knowledgeList = knowledgeMapper.selectList(queryWrapper);
        return ApiResponse.success(knowledgeList);
    }

    /**
     * 删除知识库
     */
    @Transactional
    public ApiResponse<Void> deleteKnowledge(Long userId, Long knowledgeId) {
        Knowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        if (knowledge == null || !knowledge.getUserId().equals(userId)) {
            throw new BusinessException("知识库不存在或无权操作");
        }

        knowledgeMapper.deleteById(knowledgeId);
        return ApiResponse.success();
    }

    /**
     * 上传文件到知识库
     */
    @Transactional
    public ApiResponse<Map<String, Object>> uploadFileToKnowledgeBase(
            Long userId,
            Long knowledgeId,
            String fileName,
            byte[] fileContent) {
        
        Knowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        if (knowledge == null || !knowledge.getUserId().equals(userId)) {
            throw new BusinessException("知识库不存在或无权操作");
        }

        // TODO: 实现文件上传到知识库的逻辑
        // 这里需要根据不同的provider（aliyun, milvus等）实现不同的上传逻辑
        
        Map<String, Object> result = new HashMap<>();
        result.put("knowledgeId", knowledgeId);
        result.put("fileName", fileName);
        result.put("status", "uploaded");
        
        return ApiResponse.success(result);
    }
}

