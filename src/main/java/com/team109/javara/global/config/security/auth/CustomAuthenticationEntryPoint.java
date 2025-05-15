package com.team109.javara.global.config.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team109.javara.global.common.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper; //응답할것을 josn 변경하려고 필요
    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        BaseResponse<Void> baseResponse;

        if (authException instanceof BadCredentialsException) {
            baseResponse = BaseResponse.fail("아이디 또는 비밀번호가 잘못되었습니다.", HttpStatus.UNAUTHORIZED);
        } else {
            baseResponse = BaseResponse.fail("인증이 필요합니다. 로그인을 진행해주세요.", HttpStatus.UNAUTHORIZED);
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value()); //401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); //content type 설정
        response.setCharacterEncoding("UTF-8");

        //BaseResponse을 ObjectMapper로  JSON로 변환 후 응답 본문에 넣음
        response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
    }
}
