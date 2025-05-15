package com.team109.javara.domain.vehicle.service;

import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.task.service.TaskService;
import com.team109.javara.domain.vehicle.dto.*;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import com.team109.javara.domain.vehicle.repository.WantedVehicleRepository;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppWantedVehicleService {
    //TODO: 임무에 memberId 등록해야함!!!!!!!!!

    private final WantedVehicleRepository wantedVehicleRepository;
    private final TaskService taskService;

    public List<WantedVehicleResponseDto> getAllWantedVehicles() {
        List<WantedVehicle> wantedVehicles = wantedVehicleRepository.findAllByOrderByCreatedAtDesc();
        return wantedVehicles.stream()
                .map(WantedVehicleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public WantedVehicleResponseDto getWantedVehicle(String wantedVehicleNumber) {
        WantedVehicle wantedVehicle = wantedVehicleRepository.findByWantedVehicleNumber(wantedVehicleNumber)
                .orElseThrow(() -> new GlobalException(ErrorCode.VEHICLE_NOT_FOUND));

        return WantedVehicleResponseDto.fromEntity(wantedVehicle);
    }

    @Transactional
    public WantedVehicleResponseDto registerWantedVehicle(AppWantedVehicleRequestDto requestDto) {
        WantedVehicle wantedVehicle = new WantedVehicle();

        wantedVehicle.setWantedVehicleNumber(requestDto.getWantedVehicleNumber());
        wantedVehicle.setCaseNumber(requestDto.getCaseNumber());
        wantedVehicle.setCrimeType(requestDto.getCrimeType());
        wantedVehicle.setOwnerName(requestDto.getOwnerName());
        wantedVehicle.setNotes(requestDto.getNotes());
        wantedVehicle.setWantedVehicleStatus(requestDto.getWantedVehicleStatus());

        WantedVehicle savedVehicle = wantedVehicleRepository.save(wantedVehicle);

        // 새로운 임무 생성
        taskService.createTask(savedVehicle);

        return WantedVehicleResponseDto.fromEntity(savedVehicle);
    }

    public List<WantedVehicleResponseDto> getDetection(String reporterName, Role role) {
        // meberid에 해당하면서 잡힌 목록 보여중
        List<WantedVehicle> vehicles;
        log.info("getDetection 시작 [{}], [{}]",reporterName,role);

        //경찰일 경우에 모든 정보 줌
        if (role == Role.POLICE) {
            vehicles = wantedVehicleRepository.findByLocations_ReporterName(reporterName);
            log.info("나 [{}] 발견한 차량들 {}", reporterName, vehicles);
            log.info("경찰일경우 전체 줌");
            return vehicles.stream()
                    .map(WantedVehicleResponseDto::fromEntityToPolice)
                    .collect(Collectors.toList());
        } else {
            // 일반 사용자일 경우 전체 수배 차량 조회
            vehicles = wantedVehicleRepository.findByLocations_ReporterName(reporterName);
            log.info("사용자일경우 일부만 줌");
            return vehicles.stream()
                    .map(WantedVehicleResponseDto::fromEntityToUser)
                    .collect(Collectors.toList());
        }
    }


}