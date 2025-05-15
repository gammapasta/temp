package com.team109.javara.domain.vehicle.service;

import com.team109.javara.domain.task.service.TaskService;
import com.team109.javara.domain.vehicle.dto.SearchRequestDto;
import com.team109.javara.domain.vehicle.dto.WantedVehicleRequestDto;
import com.team109.javara.domain.vehicle.dto.WantedVehicleResponseDto;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import com.team109.javara.domain.vehicle.repository.WantedVehicleRepository;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class WantedVehicleServiceImpl implements WantedVehicleService {

    private final WantedVehicleRepository wantedVehicleRepository;
    private final TaskService taskService;

    // Create
    @Transactional
    @Override
    public WantedVehicleResponseDto createWantedVehicle(WantedVehicleRequestDto requestDto) {
        // case number is unique
        if (wantedVehicleRepository.existsByCaseNumber(requestDto.getCaseNumber())) {
            throw new GlobalException(ErrorCode.DUPLICATE_CASE_NUMBER);
        }

        WantedVehicle wantedVehicle = new WantedVehicle();
        wantedVehicle.setWantedVehicleNumber(requestDto.getWantedVehicleNumber());
        wantedVehicle.setCaseNumber(requestDto.getCaseNumber());
        wantedVehicle.setCrimeType(requestDto.getCrimeType());
        wantedVehicle.setOwnerName(requestDto.getOwnerName());
        wantedVehicle.setWantedVehicleStatus(requestDto.getWantedVehicleStatus());
        wantedVehicle.setNotes(requestDto.getNotes());

        WantedVehicle saved = wantedVehicleRepository.save(wantedVehicle);

        // 새로운 임무 생성
        taskService.createTask(saved);

        return WantedVehicleResponseDto.fromEntity(saved);
    }

    // Read by Id
    @Override
    public WantedVehicleResponseDto getWantedVehicleById(Long id) {
        WantedVehicle wantedVehicle = wantedVehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WantedVehicle not found"));
        return WantedVehicleResponseDto.fromEntity(wantedVehicle);
    }

    // Read All
    @Override
    public List<WantedVehicleResponseDto> getAllWantedVehicles() {
        return wantedVehicleRepository.findAll().stream()
                .map(WantedVehicleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Update
    @Override
    public WantedVehicleResponseDto updateWantedVehicle(Long id, WantedVehicleRequestDto requestDto) {
        WantedVehicle wantedVehicle = wantedVehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WantedVehicle not found"));

        // 수정시 case-number 중복 검사
        boolean caseNumberExists = wantedVehicleRepository.existsByCaseNumberAndWantedVehicleIdNot(requestDto.getCaseNumber(), id);
        if (caseNumberExists) {
            throw new GlobalException(ErrorCode.DUPLICATE_CASE_NUMBER);
        }

        wantedVehicle.setWantedVehicleNumber(requestDto.getWantedVehicleNumber());
        wantedVehicle.setCaseNumber(requestDto.getCaseNumber());
        wantedVehicle.setCrimeType(requestDto.getCrimeType());
        wantedVehicle.setOwnerName(requestDto.getOwnerName());
        wantedVehicle.setWantedVehicleStatus(requestDto.getWantedVehicleStatus());
        wantedVehicle.setNotes(requestDto.getNotes());

        WantedVehicle updated = wantedVehicleRepository.save(wantedVehicle);
        return WantedVehicleResponseDto.fromEntity(updated);
    }

    // Delete by Id
    @Override
    public void deleteWantedVehicle(Long id) {
        wantedVehicleRepository.deleteById(id);
    }



    @Override
    public Page<WantedVehicleResponseDto> searchVehicles(SearchRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        WantedVehicleStatus status = null;
        if (searchRequest.getWantedVehicleStatus() != null && !searchRequest.getWantedVehicleStatus().isBlank()) {
            try {
                status = WantedVehicleStatus.valueOf(searchRequest.getWantedVehicleStatus().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("유효하지 않은 상태 값입니다: " + searchRequest.getWantedVehicleStatus());
            }
        }

        Page<WantedVehicle> vehicles = wantedVehicleRepository.searchByFilters(
                searchRequest.getWantedVehicleNumber(),
                searchRequest.getCaseNumber(),
                searchRequest.getCrimeType(),
                searchRequest.getOwnerName(),
                status,
                searchRequest.getDeviceId(),
                pageable
        );

        return vehicles.map(WantedVehicleResponseDto::fromEntityToPolice);
    }


}
