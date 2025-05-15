package com.team109.javara.domain.location.service;

import com.team109.javara.domain.location.dto.PoliceLocationRequestDto;
import com.team109.javara.domain.location.dto.PoliceLocationResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface PoliceLocationService {

    PoliceLocationResponseDto createLocation(PoliceLocationRequestDto requestDto);

    List<PoliceLocationResponseDto> getAllLocations();

    PoliceLocationResponseDto getLocationById(Long id);

    List<PoliceLocationResponseDto> getLocationsByMemberId(Long memberId);

    List<PoliceLocationResponseDto> getLocationsByCreatedAtBetween(LocalDate startDate, LocalDate endDate);
}

