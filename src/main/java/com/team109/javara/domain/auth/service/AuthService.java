package com.team109.javara.domain.auth.service;

import com.team109.javara.domain.member.dto.MemberInfoResponseDto;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.auth.entity.RefreshToken;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.auth.repository.RefreshTokenRepository;
import com.team109.javara.domain.auth.dto.LoginRequestDto;
import com.team109.javara.domain.auth.dto.SignupRequestDto;
import com.team109.javara.domain.auth.dto.TokenResponseDto;
import com.team109.javara.domain.auth.jwt.JwtTokenInfo;
import com.team109.javara.domain.auth.jwt.JwtTokenProvider;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor

public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 로그인 처리
    public TokenResponseDto login(@Valid LoginRequestDto loginRequestDto) {
        log.debug("로그인 서버스 시작");
        
        try{
            
            //1. 인증 객체 생성
            Authentication authentication = authenticationManager.authenticate(  //AuthenticationManager로 인증
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),  //여기서 policeId 라서 나중에 바꿔야함
                            loginRequestDto.getPassword()
                    )
            );
            log.info("인증 객체 생성 완료");

            //2. SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("인증 정보 저장 완료");

            //3. JWT 토큰 생성
            JwtTokenInfo jwtTokenInfo = jwtTokenProvider.createToken(authentication);
            log.info("토큰 생성 완료");



            refreshTokenRepository.findByUsername(loginRequestDto.getUsername())
                    .ifPresentOrElse(
                            existingToken -> {
                                existingToken.updateToken(jwtTokenInfo.refreshToken());
                                refreshTokenRepository.save(existingToken);
                                log.info("RefreshToken 갱신");
                            },
                            () -> {
                                RefreshToken newToken = RefreshToken.builder()
                                        .username(loginRequestDto.getUsername())
                                        .refreshToken(jwtTokenInfo.refreshToken())
                                        .build();
                                refreshTokenRepository.save(newToken);
                                log.info("RefreshToken 새로 저장");
                            }
                    );

            //4. 토큰과 로그인 성보 정보를 dto로 반환
            return TokenResponseDto.builder()
                    .accessToken(jwtTokenInfo.accessToken())
                    .refreshToken(jwtTokenInfo.refreshToken())
                    .username(loginRequestDto.getUsername())
                    .build();

        }catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }

    //언제써야할지 모르겠음
    public TokenResponseDto refreshAccessToken(String refreshToken) {
        try {

            //refresh token에서 유저 이름 가져오기
            Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
            String username = authentication.getName();

            //username으로 refresh token 조회
            RefreshToken storedToken = refreshTokenRepository.findByUsername(username).orElse(null);

            // 3. DB 검증
            if (storedToken == null) {
                log.warn("DB에 refresh Token 없음. 사용자: {}", username);
                throw new GlobalException(ErrorCode.INVALID_REFRESH_TOKEN, ("DB에 refresh token 없습니다."));
            }

            // 4. 사용자에서온 refresh token과 DB에 저장된 토큰이 일치하는지 확인
            if (!storedToken.getRefreshToken().equals(refreshToken)) {
                log.warn("refresh token과 db에 있는 refresh token 불일치. 사용자: {}", username);
                refreshTokenRepository.delete(storedToken);
                throw new GlobalException(ErrorCode.TOKEN_MISMATCH, ("refresh token 불일치"));
            }
            String newAccessToken = jwtTokenProvider.createNewAccessToken(refreshToken);
            return TokenResponseDto.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Keep the existing refresh token
                    .username(username)
                    .build();
            }catch (GlobalException e) {
                log.warn("토큰 갱신 중 로직 오류: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("토큰 갱신 실패: {}", e.getMessage(), e);
                throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 갱신 중 예상치 못한 오류 발생", e);
            }
    }


    public boolean checkRefreshToken(String username) {
        if(refreshTokenRepository.existsByUsername(username)){
            log.info("로그아웃: 사람 존재");
            return true;
        }
        log.info("로그아웃: 사람 없음");
        return false;
    }

    //회원가입 처리
    @Transactional
    public MemberInfoResponseDto register(@Valid SignupRequestDto signupRequestDto) {
        String policeId = null;

        // 사용자명 중복 확인
        if (memberRepository.existsByUsername(signupRequestDto.getUsername())) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }
        // 사용자명 중복 확인
        if (memberRepository.existsByPoliceId(signupRequestDto.getPoliceId()) && !signupRequestDto.getPoliceId().isBlank()) {
            throw new IllegalStateException("이미 사용 중인 경찰id입니다.");
        }

        Role memberRole = Role.USER; // 기본값


        if (signupRequestDto.getPoliceId() != null && !signupRequestDto.getPoliceId().isBlank()) {
            memberRole = Role.POLICE; // 기본값
            policeId = signupRequestDto.getPoliceId();
        }


        // Member 엔티티 생성
        Member newMember = Member.builder()
                .username(signupRequestDto.getUsername())
                .password(passwordEncoder.encode(signupRequestDto.getPassword())) // 비밀번호 암호화!
                .name(signupRequestDto.getName())
                .gender(signupRequestDto.getGender())
                .role(memberRole)
                .memberStatus(MemberStatus.INACTIVE)
                .penaltyPoints(BigDecimal.ZERO)
                .policeId(policeId)
                .createdAt(LocalDateTime.now())
                .build();


        //Member 엔티티 저장
        Member savedMember = memberRepository.save(newMember);

        // 저장된 Member 엔티티를 응답 DTO로 변환하여 반환
        return MemberInfoResponseDto.fromEntity(savedMember);
    }


    // logout
    @Transactional
    public void logout(String username) {
        try {
            refreshTokenRepository.deleteByUsername(username);
            log.info("refresh token 삭제: member [{}]", username);
        } catch (Exception e) {
            log.error("로그아웃 실패: member {}", username, e);
            throw new RuntimeException("로그아웃 실패", e);
        }
    }


}