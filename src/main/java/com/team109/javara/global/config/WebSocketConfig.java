package com.team109.javara.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 이 어노테이션 확인
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 엔드포인트가 "/ws"로 올바르게 설정되었는지 확인
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
        ;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // 브로커 목적지
        config.setApplicationDestinationPrefixes("/app"); // @MessageMapping 핸들러
        config.setUserDestinationPrefix("/user"); // 사용자 목적지 접두사

    }
}