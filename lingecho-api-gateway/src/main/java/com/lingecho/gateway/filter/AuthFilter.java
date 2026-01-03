package com.lingecho.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 统一鉴权过滤器
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    // 不需要鉴权的路径
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/reset-password",
            "/api/auth/reset-password/confirm"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 检查是否为公开路径
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 获取 Token
        String token = getToken(request);
        if (token == null || token.isEmpty()) {
            return unauthorized(exchange);
        }

        // 验证 Token（这里可以调用 auth-service 验证，或使用 JWT）
        // 为了简化，这里只做基本检查
        if (!isValidToken(token)) {
            return unauthorized(exchange);
        }

        // 将用户ID添加到请求头中，供下游服务使用
        Long userId = getUserIdFromToken(token);
        if (userId != null) {
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .build();
            exchange = exchange.mutate().request(modifiedRequest).build();
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private String getToken(ServerHttpRequest request) {
        // 从 Header 中获取
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return authHeader;
        }

        // 从 Query 参数中获取
        String token = request.getQueryParams().getFirst("token");
        return token;
    }

    private boolean isValidToken(String token) {
        // TODO: 实现 Token 验证逻辑
        // 可以调用 auth-service 验证，或使用 JWT 验证
        return token != null && !token.isEmpty();
    }

    private Long getUserIdFromToken(String token) {
        // TODO: 从 Token 中解析用户ID
        // 这里需要实现 JWT 解析逻辑
        try {
            // 简化实现，实际应该解析 JWT
            return 1L;
        } catch (Exception e) {
            log.error("解析Token失败", e);
            return null;
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

