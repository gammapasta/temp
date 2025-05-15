package com.team109.javara.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TokenResponseDto {
    private String username;
    private String accessToken;
    private String refreshToken;
}