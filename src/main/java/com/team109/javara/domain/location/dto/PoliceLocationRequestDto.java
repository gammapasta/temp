package com.team109.javara.domain.location.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PoliceLocationRequestDto {
    private Long memberId;
    private BigDecimal latitude;
    private BigDecimal longitude;
}