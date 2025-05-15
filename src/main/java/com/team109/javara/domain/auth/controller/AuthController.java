package com.team109.javara.domain.auth.controller;

import com.team109.javara.domain.member.dto.MemberInfoResponseDto;
import com.team109.javara.global.common.response.BaseResponse;
import com.team109.javara.domain.auth.dto.LoginRequestDto;
import com.team109.javara.domain.auth.dto.RefreshRequestDto;
import com.team109.javara.domain.auth.dto.SignupRequestDto;
import com.team109.javara.domain.auth.dto.TokenResponseDto;
import com.team109.javara.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    // 성공 시 BaseResponse 직접 반환 (기본 200 OK 상태 코드)
    public BaseResponse<TokenResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);
        log.info("로그인 성공: {}", loginRequestDto.getUsername()); // 성공 로그 변경
        return BaseResponse.success("로그인 성공", tokenResponseDto);
    }


    @PostMapping("/signup")
    // 회원가입 성공 시 201 Created 상태 코드를 명시적으로 반환하기 위해 ResponseEntity 사용
    public BaseResponse<MemberInfoResponseDto> registerUser(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        MemberInfoResponseDto memberInfoResponseDto = authService.register(signupRequestDto);
        log.info("회원가입 성공: {}", signupRequestDto.getPoliceId());

        return BaseResponse.success("사용자 등록 성공!", HttpStatus.CREATED, memberInfoResponseDto);
    }


    @PostMapping("/refresh")
    public BaseResponse<TokenResponseDto> refresh(@Valid  @RequestBody RefreshRequestDto refreshRequestDto) {
        TokenResponseDto tokenResponseDto = authService.refreshAccessToken(refreshRequestDto.getRefreshToken());

        return BaseResponse.success("AccesssToken 재발급 성공, 새로운 AccesssToken을 사용하세요", HttpStatus.CREATED, tokenResponseDto);
    }

    @PostMapping("/logout")
    public BaseResponse<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            if(authService.checkRefreshToken(userDetails.getUsername())) {
                authService.logout(userDetails.getUsername());
                return BaseResponse.success("로그아웃 완료", null);
            }
        }
        return BaseResponse.fail("로그아웃 실패: 인증 정보가 없습니다.", HttpStatus.FORBIDDEN);
    }
}