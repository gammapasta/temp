package com.team109.javara.domain.location.dto;

import com.team109.javara.domain.location.entity.PoliceLocation;
import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import com.team109.javara.domain.member.entity.Member;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PLResponseDto {
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static PLResponseDto fromEntity(Member member) {
        return PLResponseDto.builder()
                .latitude(member.getLocations()
                        .stream()
                        .max(Comparator.comparing(PoliceLocation::getCreatedAt))
                        .map(PoliceLocation::getLatitude)
                        .orElse(null))
                .longitude(member.getLocations()
                        .stream()
                        .max(Comparator.comparing(PoliceLocation::getCreatedAt))
                        .map(PoliceLocation::getLongitude)
                        .orElse(null))
                .build();

    }
}