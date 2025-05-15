package com.team109.javara.domain.vehicle.dto;

import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequestDto {
    private String wantedVehicleNumber;
    private String caseNumber;
    private String crimeType;
    private String ownerName;
    private String wantedVehicleStatus;
    private String deviceId;

    // 페이지네이션 기본값 설정
    private int page = 0;  // 0번째 페이지 (주의: 1페이지 → 0)
    private int size = 10; // 페이지당 10개
}
