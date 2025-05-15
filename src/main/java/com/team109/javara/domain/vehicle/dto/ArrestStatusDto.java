package com.team109.javara.domain.vehicle.dto;

import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArrestStatusDto {
    @NotBlank(message = "수배차량 번호는 필수 항목입니다.")
    private String wantedVehicleNumber;

    @NotNull(message = "수배차량 상태는 필수 항목 ENUM 입니다.")
    private WantedVehicleStatus wantedVehicleStatus;
}
