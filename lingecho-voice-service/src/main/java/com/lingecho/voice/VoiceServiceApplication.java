package com.lingecho.voice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 语音服务（WebSocket/WebRTC）
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class VoiceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoiceServiceApplication.class, args);
    }
}

