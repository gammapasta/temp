package com.team109.javara.domain.edgeDevice.dto;

import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationResponseDto {
    public String wantedVehicleNumber;
    public WantedVehicleStatus wantedVehicleStatus;

}
