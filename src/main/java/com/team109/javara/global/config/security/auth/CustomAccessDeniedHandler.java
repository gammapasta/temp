package com.team109.javara.global.config.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team109.javara.global.common.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper; //응답할것을 josn 변경하려고 필요
    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        BaseResponse<Void> baseResponse = BaseResponse.fail("해당 리소스에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN);


        response.setStatus(HttpStatus.FORBIDDEN.value()); //403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");


        // BaseResponse 객체를 JSON 문자열로 변환하여 응답 본문에 작성
        response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
    }
}
