package com.team109.javara.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.file.directory}")
    private String fileDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /images/** 요청 → 실제 파일 시스템의 경로로 매핑
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + fileDirectory); // 꼭 끝에 슬래시 `/` 붙이기
    }
}
