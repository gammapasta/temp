package com.team109.javara.domain.vehicle.dto;

import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppWantedVehicleRequestDto {

    @NotBlank(message = "수배차량 번호는 필수 항목입니다.")
    private String wantedVehicleNumber;

    @NotBlank(message = "사건 번호는 필수 항목입니다.")
    private String caseNumber;

    @NotBlank(message = "범죄 유형은 필수 항목입니다.")
    private String crimeType;

    @NotBlank(message = "피해자 성함은 필수 항목입니다.")
    private String ownerName;

    @NotNull(message = "enum 타입이 필요합니다.")
    private WantedVehicleStatus wantedVehicleStatus;

    private String notes;
}