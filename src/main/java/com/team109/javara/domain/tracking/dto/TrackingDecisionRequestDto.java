package com.team109.javara.domain.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrackingDecisionRequestDto {
    @NotBlank(message = "맴버id는 항목입니다.")
    private Long memberId;
    @NotBlank(message = "taskId는 필수입니다")
    private Long taskId;
    @NotBlank(message = "수배차량 번호는 필수 항목입니다.")
    private String wantedVehicleNumber;
    @NotNull(message = "enum(ACCEPTED,REJECTED) 타입이 필요합니다.")
    private Decision decision;


    public enum Decision {
        ACCEPTED,
        REJECTED
    }
}
