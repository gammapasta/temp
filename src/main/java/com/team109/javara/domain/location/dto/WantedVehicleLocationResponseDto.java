package com.team109.javara.domain.location.dto;

import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WantedVehicleLocationResponseDto {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String vehicleNumber;
    private WantedVehicleStatus wantedVehicleStatus;

    public static WantedVehicleLocationResponseDto fromEntity(WantedVehicleLocation entity) {
        return WantedVehicleLocationResponseDto.builder()
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .vehicleNumber(entity.getWantedVehicle().getWantedVehicleNumber())
                .wantedVehicleStatus(entity.getWantedVehicle().getWantedVehicleStatus())
                .build();
    }
}
