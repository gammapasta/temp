package com.team109.javara.domain.vehicle.controller;

import com.team109.javara.domain.vehicle.dto.SearchRequestDto;
import com.team109.javara.domain.vehicle.dto.WantedVehicleRequestDto;
import com.team109.javara.domain.vehicle.dto.WantedVehicleResponseDto;
import com.team109.javara.domain.vehicle.service.WantedVehicleService;
import com.team109.javara.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/wanted-vehicles")
@RequiredArgsConstructor
public class WantedVehicleController {

    private final WantedVehicleService wantedVehicleService;

    @Operation(summary = "Admin 수배차량 등록", description = "새로운 수배차량을 등록합니다.")
    @PostMapping
    public BaseResponse<WantedVehicleResponseDto> createWantedVehicle(@Valid @RequestBody WantedVehicleRequestDto requestDto) {
        WantedVehicleResponseDto response = wantedVehicleService.createWantedVehicle(requestDto);
        return BaseResponse.success("수배 차량 등록 성공", response);
    }

    @GetMapping
    public BaseResponse<List<WantedVehicleResponseDto>> getAllWantedVehicles() {
        List<WantedVehicleResponseDto> responses = wantedVehicleService.getAllWantedVehicles();
        return BaseResponse.success("전체 수배 차량 조회 성공", responses);
    }

    @GetMapping("/{id}")
    public BaseResponse<WantedVehicleResponseDto> getWantedVehicleById(@PathVariable Long id) {
        WantedVehicleResponseDto response = wantedVehicleService.getWantedVehicleById(id);
        return BaseResponse.success("수배 차량 조회 성공", response);
    }

    @PutMapping("/{id}")
    public BaseResponse<WantedVehicleResponseDto> updateWantedVehicle(
            @PathVariable Long id,
            @RequestBody WantedVehicleRequestDto requestDto) {
        WantedVehicleResponseDto response = wantedVehicleService.updateWantedVehicle(id, requestDto);
        return BaseResponse.success("수배 차량 수정 성공", response);
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteWantedVehicle(@PathVariable Long id) {
        wantedVehicleService.deleteWantedVehicle(id);
        return BaseResponse.success("수배 차량 삭제 성공", null);
    }

    @PostMapping("/search")
    public BaseResponse<Page<WantedVehicleResponseDto>> searchWantedVehicles(
            @RequestBody SearchRequestDto searchRequest
    ) {
        Page<WantedVehicleResponseDto> result = wantedVehicleService.searchVehicles(searchRequest);
        return BaseResponse.success("검색 성공", result);
    }

}
