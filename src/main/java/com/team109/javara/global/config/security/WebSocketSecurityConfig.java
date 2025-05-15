package com.team109.javara.global.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpTypeMatchers(SimpMessageType.CONNECT).permitAll() // 일단 모든 CONNECT 허용 (나중에 필요시 변경)
                .anyMessage().permitAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true; // CSRF 비활성화 (SecurityConfig 설정과 일치시킴)
    }
}