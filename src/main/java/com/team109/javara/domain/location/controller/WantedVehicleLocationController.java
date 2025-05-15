package com.team109.javara.domain.location.controller;

import com.team109.javara.domain.location.dto.WantedVehicleLocationResponseDto;
import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import com.team109.javara.domain.location.service.WantedVehicleLocationService;
import com.team109.javara.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class WantedVehicleLocationController {
    private final WantedVehicleLocationService locationService;

    @GetMapping("/wanted-vehicles")
    public BaseResponse<List<WantedVehicleLocationResponseDto>> getWantedVehiclesInViewport(
            @RequestParam double centerLat,
            @RequestParam double centerLng,
            @RequestParam int zoomLevel
    ) {
        List<WantedVehicleLocationResponseDto> result = locationService.findVehiclesInViewport(centerLat, centerLng, zoomLevel);
        return BaseResponse.success("조회성공", result);
    }

    @GetMapping("/wanted-vehicles/{vehicleNumber}")
    public BaseResponse<WantedVehicleLocationResponseDto> getWantedVehiclesByVehicleNumber(
            @PathVariable String vehicleNumber
    ) {
        WantedVehicleLocationResponseDto result = locationService.findVehiclesByVehicleNumber(vehicleNumber);
        return BaseResponse.success("조회성공", result);
    }
}
