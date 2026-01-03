package com.lingecho.assistant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingecho.assistant.dto.CreateAssistantRequest;
import com.lingecho.assistant.dto.UpdateAssistantRequest;
import com.lingecho.assistant.entity.Assistant;
import com.lingecho.assistant.mapper.AssistantMapper;
import com.lingecho.common.core.ApiResponse;
import com.lingecho.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 助手服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantService {

    private final AssistantMapper assistantMapper;

    /**
     * 创建助手
     */
    @Transactional
    public ApiResponse<Assistant> createAssistant(Long userId, CreateAssistantRequest request) {
        Assistant assistant = Assistant.builder()
                .userId(userId)
                .groupId(request.getGroupId())
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .systemPrompt("empty system prompt")
                .personaTag("mentor")
                .temperature(0.6f)
                .maxTokens(150)
                .jsSourceId(UUID.randomUUID().toString().replace("-", ""))
                .language("zh-cn")
                .speaker("101016")
                .enableGraphMemory(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assistantMapper.insert(assistant);
        return ApiResponse.success(assistant);
    }

    /**
     * 获取助手列表
     */
    public ApiResponse<List<Assistant>> listAssistants(Long userId) {
        LambdaQueryWrapper<Assistant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Assistant::getUserId, userId)
                .orderByDesc(Assistant::getCreatedAt);
        List<Assistant> assistants = assistantMapper.selectList(queryWrapper);
        return ApiResponse.success(assistants);
    }

    /**
     * 获取助手详情
     */
    public ApiResponse<Assistant> getAssistant(Long assistantId, Long userId) {
        Assistant assistant = assistantMapper.selectById(assistantId);
        if (assistant == null) {
            throw new BusinessException("助手不存在");
        }
        // 检查权限
        if (!assistant.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该助手");
        }
        return ApiResponse.success(assistant);
    }

    /**
     * 更新助手
     */
    @Transactional
    public ApiResponse<Assistant> updateAssistant(Long assistantId, Long userId, UpdateAssistantRequest request) {
        Assistant assistant = assistantMapper.selectById(assistantId);
        if (assistant == null) {
            throw new BusinessException("助手不存在");
        }
        // 检查权限
        if (!assistant.getUserId().equals(userId)) {
            throw new BusinessException("无权修改该助手");
        }

        LambdaUpdateWrapper<Assistant> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Assistant::getId, assistantId);

        if (request.getName() != null) {
            updateWrapper.set(Assistant::getName, request.getName());
        }
        if (request.getDescription() != null) {
            updateWrapper.set(Assistant::getDescription, request.getDescription());
        }
        if (request.getIcon() != null) {
            updateWrapper.set(Assistant::getIcon, request.getIcon());
        }
        if (request.getSystemPrompt() != null) {
            updateWrapper.set(Assistant::getSystemPrompt, request.getSystemPrompt());
        }
        if (request.getPersonaTag() != null) {
            updateWrapper.set(Assistant::getPersonaTag, request.getPersonaTag());
        }
        if (request.getTemperature() != null) {
            updateWrapper.set(Assistant::getTemperature, request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            updateWrapper.set(Assistant::getMaxTokens, request.getMaxTokens());
        }
        if (request.getLanguage() != null) {
            updateWrapper.set(Assistant::getLanguage, request.getLanguage());
        }
        if (request.getSpeaker() != null) {
            updateWrapper.set(Assistant::getSpeaker, request.getSpeaker());
        }
        if (request.getVoiceCloneId() != null) {
            updateWrapper.set(Assistant::getVoiceCloneId, request.getVoiceCloneId());
        }
        if (request.getKnowledgeBaseId() != null) {
            updateWrapper.set(Assistant::getKnowledgeBaseId, request.getKnowledgeBaseId());
        }
        if (request.getTtsProvider() != null) {
            updateWrapper.set(Assistant::getTtsProvider, request.getTtsProvider());
        }
        if (request.getApiKey() != null) {
            updateWrapper.set(Assistant::getApiKey, request.getApiKey());
        }
        if (request.getApiSecret() != null) {
            updateWrapper.set(Assistant::getApiSecret, request.getApiSecret());
        }
        if (request.getLlmModel() != null) {
            updateWrapper.set(Assistant::getLlmModel, request.getLlmModel());
        }
        if (request.getEnableGraphMemory() != null) {
            updateWrapper.set(Assistant::getEnableGraphMemory, request.getEnableGraphMemory());
        }

        updateWrapper.set(Assistant::getUpdatedAt, LocalDateTime.now());
        assistantMapper.update(null, updateWrapper);

        Assistant updatedAssistant = assistantMapper.selectById(assistantId);
        return ApiResponse.success(updatedAssistant);
    }

    /**
     * 删除助手
     */
    @Transactional
    public ApiResponse<Void> deleteAssistant(Long assistantId, Long userId) {
        Assistant assistant = assistantMapper.selectById(assistantId);
        if (assistant == null) {
            throw new BusinessException("助手不存在");
        }
        // 检查权限
        if (!assistant.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该助手");
        }

        assistantMapper.deleteById(assistantId);
        return ApiResponse.success();
    }
}

