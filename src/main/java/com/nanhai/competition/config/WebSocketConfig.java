package com.nanhai.competition.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // 启用简单消息代理，消息前缀为/topic和/queue
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用程序目的地前缀为/app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // 注册STOMP端点 - 匹配 /nanhai/api 上下文路径
        registry.addEndpoint("/ws/competition")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 启用SockJS支持
    }
}

