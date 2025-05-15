package com.team109.javara.global.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//JWT 설정
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtConfig {
    private String secretKey;
    private long accessExpiration;
    private long refreshExpiration;
}