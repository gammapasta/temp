package com.team109.javara.domain.vehicle.service;

import com.team109.javara.domain.vehicle.dto.SearchRequestDto;
import com.team109.javara.domain.vehicle.dto.WantedVehicleRequestDto;
import com.team109.javara.domain.vehicle.dto.WantedVehicleResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WantedVehicleService {

    WantedVehicleResponseDto createWantedVehicle(WantedVehicleRequestDto requestDto);

    WantedVehicleResponseDto getWantedVehicleById(Long id);

    List<WantedVehicleResponseDto> getAllWantedVehicles();

    WantedVehicleResponseDto updateWantedVehicle(Long id, WantedVehicleRequestDto requestDto);

    void deleteWantedVehicle(Long id);

    Page<WantedVehicleResponseDto> searchVehicles(SearchRequestDto searchRequest);
}
