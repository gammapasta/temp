package com.team109.javara.global.config.security;

import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.global.config.security.auth.CustomAccessDeniedHandler;
import com.team109.javara.global.config.security.auth.CustomAuthenticationEntryPoint;
import com.team109.javara.domain.auth.jwt.JwtAuthenticationFilter;
import com.team109.javara.domain.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headersConfigurer ->headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // jwt 토큰을 사용하기 때문에 세션을 STATELESS로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize -> authorize
                        //swagger
                        .requestMatchers("/v3/api-docs","/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources", "/swagger-resources/**", "/configuration/ui", "/configuration/security", "/webjars/**").permitAll()

                        //login 관련
                        .requestMatchers("/api/auth/login", "/api/auth/signup").permitAll()

                        //웹소켓
                        .requestMatchers("/ws/**", "/ws-stomp/**").permitAll()

                        //test
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/api/edge-devices/**").permitAll()
                        .requestMatchers("/api/edge-devices/verification").permitAll()
                        .requestMatchers("/images/**").permitAll()

                        //어드민만 들어갈 수 있음
                        .requestMatchers("/admin/**").hasAuthority(Role.ADMIN.getRoleName())
                        .anyRequest().authenticated()
                )

                //JWT 필터
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                //JWT가 없거나, 잘못됐거나, 만료됐거나 로그인 필요
                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                //JWT는 유효하지만, 해당 리소스 접근 권한 없는 상황
                                .accessDeniedHandler(customAccessDeniedHandler)
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
            return authenticationConfiguration.getAuthenticationManager();
    }

}
