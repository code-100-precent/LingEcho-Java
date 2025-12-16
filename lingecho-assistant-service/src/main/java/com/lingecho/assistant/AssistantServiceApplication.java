package com.lingecho.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI 助手服务
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class AssistantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssistantServiceApplication.class, args);
    }
}

