package com.team109.javara.domain.vehicle.dto;

import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EdgeDeviceWantedVehicleResponseDto {
    private Long wantedVehicleId;
    private String wantedVehicleNumber;
    private WantedVehicleStatus wantedVehicleStatus;

    public static EdgeDeviceWantedVehicleResponseDto fromEntity(WantedVehicle wantedVehicle) {
        return EdgeDeviceWantedVehicleResponseDto.builder()
                .wantedVehicleId(wantedVehicle.getWantedVehicleId())
                .wantedVehicleNumber(wantedVehicle.getWantedVehicleNumber())
                .wantedVehicleStatus(wantedVehicle.getWantedVehicleStatus())
                .build();
    }

}
