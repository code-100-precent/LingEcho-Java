package com.lingecho.voice.controller;

import com.lingecho.common.core.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

/**
 * 语音服务控制器
 */
@RestController
@RequestMapping("/voice")
@RequiredArgsConstructor
public class VoiceController {

    @PostMapping("/oneshot")
    public Result<VoiceResponse> oneshot(@RequestBody VoiceRequest request) {
        // TODO: 实现一次性语音识别和合成
        VoiceResponse response = new VoiceResponse();
        response.setText("Mock response");
        return Result.success(response);
    }

    @PostMapping("/call")
    public Result<CallResponse> startCall(@RequestBody CallRequest request) {
        // TODO: 实现实时语音通话
        CallResponse response = new CallResponse();
        response.setSessionId("mock-session-id");
        return Result.success(response);
    }

    @MessageMapping("/audio")
    @SendTo("/topic/voice")
    public AudioMessage handleAudio(AudioMessage message) {
        // TODO: 实现 WebSocket 音频消息处理
        return message;
    }

    @Data
    static class VoiceRequest {
        private String audio;
        private String language;
    }

    @Data
    static class VoiceResponse {
        private String text;
        private String audio;
    }

    @Data
    static class CallRequest {
        private Long assistantId;
        private String language;
    }

    @Data
    static class CallResponse {
        private String sessionId;
        private String wsUrl;
    }

    @Data
    static class AudioMessage {
        private String sessionId;
        private byte[] audioData;
        private String type;
    }
}

