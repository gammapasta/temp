package com.team109.javara.domain.location.controller;

import com.team109.javara.domain.location.dto.PoliceLocationRequestDto;
import com.team109.javara.domain.location.dto.PoliceLocationResponseDto;
import com.team109.javara.domain.location.service.PoliceLocationService;
import com.team109.javara.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/location/police")
@RequiredArgsConstructor
public class PoliceLocationController {

    private final PoliceLocationService policeLocationService;

    @PostMapping
    public BaseResponse<PoliceLocationResponseDto> createLocation(
            @RequestBody PoliceLocationRequestDto requestDto) {
        PoliceLocationResponseDto response = policeLocationService.createLocation(requestDto);
        return BaseResponse.success("생성 성공", response);
    }

    @GetMapping
    public BaseResponse<List<PoliceLocationResponseDto>> getAllLocations() {
        List<PoliceLocationResponseDto> response = policeLocationService.getAllLocations();
        return BaseResponse.success("전체 조회 성공", response);
    }

    @GetMapping("/{id}")
    public BaseResponse<PoliceLocationResponseDto> getLocationById(@PathVariable Long id) {
        PoliceLocationResponseDto response = policeLocationService.getLocationById(id);
        return BaseResponse.success("단일 조회 성공", response);
    }

    @GetMapping("/member/{memberId}")
    public BaseResponse<List<PoliceLocationResponseDto>> getLocationsByMemberId(@PathVariable Long memberId) {
        List<PoliceLocationResponseDto> response = policeLocationService.getLocationsByMemberId(memberId);
        return BaseResponse.success("멤버별 조회 성공", response);
    }

    @GetMapping("/date")
    public BaseResponse<List<PoliceLocationResponseDto>> getLocationsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<PoliceLocationResponseDto> response =
                policeLocationService.getLocationsByCreatedAtBetween(startDate, endDate);

        return BaseResponse.success("기간별 조회 성공", response);
    }
}
