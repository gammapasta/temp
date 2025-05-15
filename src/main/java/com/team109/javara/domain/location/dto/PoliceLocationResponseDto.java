package com.team109.javara.domain.location.dto;

import com.team109.javara.domain.location.entity.PoliceLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PoliceLocationResponseDto {
    private Long id;
    private Long memberId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;

    public static PoliceLocationResponseDto fromEntity(PoliceLocation location) {
        return new PoliceLocationResponseDto(
                location.getId(),
                location.getMember().getId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCreatedAt()
        );
    }
}
