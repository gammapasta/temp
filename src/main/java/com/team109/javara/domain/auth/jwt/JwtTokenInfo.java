package com.team109.javara.domain.auth.jwt;

import lombok.Builder;

@Builder
public record JwtTokenInfo (String accessToken, String refreshToken){

}
