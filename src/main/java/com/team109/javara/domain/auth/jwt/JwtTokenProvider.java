package com.team109.javara.domain.auth.jwt;

import com.team109.javara.global.config.security.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {


    //처음 서버 실행될떄
    private static final String AUTHORITIES_KEY = "auth";
    private final Key key;  //서버 종료 전까지 key 고정
    private final long accessTokenExpiration; // 만료 시간 필드
    private final long refreshTokenExpiration;

    // JwtTokenProvider 빈이 생성될 때 한번반 만들어서 재사용함
    public JwtTokenProvider(JwtConfig jwtConfig) {
        byte[] keyBytes = jwtConfig.getSecretKey().getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes); // Key 객체 생성
        this.accessTokenExpiration = jwtConfig.getAccessExpiration();
        this.refreshTokenExpiration = jwtConfig.getRefreshExpiration();
    }

    public JwtTokenInfo jwtTokenInfo(String accessToken, String refreshToken) {
        return null;
    }

    // 토큰 생성
    public JwtTokenInfo createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date accessTokenValidity = new Date(now + accessTokenExpiration);
        Date refreshTokenValidity = new Date(now + refreshTokenExpiration);

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setIssuedAt(new Date())
                .setExpiration(accessTokenValidity)
                .compact();
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setIssuedAt(new Date())
                .setExpiration(refreshTokenValidity)
                .compact();


        return new JwtTokenInfo(accessToken, refreshToken);
    }


    //refresh token으로 access token 생성하기
    public String createNewAccessToken(String refreshToken) {
        //1. refresh token 확인
        if (!validateToken(refreshToken)) {
            throw new JwtException("Invalid refresh token");
        }

        //2. refresh token에서 authentication 가져옴
        Authentication authentication = getAuthentication(refreshToken);

        //3. 새로운 access token 만들기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .compact();
    }


    // 토큰에서 Authentication 추출
    public Authentication getAuthentication(String token) {
        //권한 정보를 읽기, token의 payload (key value) 들
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        //Claim에서 권한 정보를 읽어 SimpleGrantedAuthority 컬렉션으로 변환 -> 해당하는 아이디에
        Collection<? extends GrantedAuthority> authorities;
        String authoritiesString = claims.get(AUTHORITIES_KEY, String.class);

        if (authoritiesString != null && !authoritiesString.trim().isEmpty()) {
            authorities = Arrays.stream(authoritiesString.split(","))   //claims에서 auth(ROLE) 가져와서 분리
                        .filter(auth -> !auth.trim().isEmpty())
                        .map(SimpleGrantedAuthority::new)  //"ROLE_USER" -> SimpleGrantedAuthority("ROLE_USER")
                        .collect(Collectors.toList());
        } else {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        UserDetails principal = new User(claims.getSubject(), "", authorities);  //스프링의 기본 User 클래스 사용//jwt에 비밀번호 없어서 빈 문자 넣음

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

}