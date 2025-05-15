package com.team109.javara.domain.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrackingDecisionResponseDto {
    private Long memberId;
    private Long taskId;
    private String wantedVehicleNumber;
}
