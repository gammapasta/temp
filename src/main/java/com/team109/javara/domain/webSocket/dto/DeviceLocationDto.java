package com.team109.javara.domain.webSocket.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLocationDto {
    private String edgeDeviceId;
    //private MemberStatus memberStatus;
    private Long wantedVehicleId;

    //순서 위도 경도 iso6709
    @NotNull(message = "위도는 필수 값입니다.")
    @Digits(integer = 4, fraction = 6)
    private BigDecimal latitude;

    @NotNull(message = "경도는 필수 값입니다.")
    @Digits(integer = 4, fraction = 6)
    private BigDecimal longitude;
}
