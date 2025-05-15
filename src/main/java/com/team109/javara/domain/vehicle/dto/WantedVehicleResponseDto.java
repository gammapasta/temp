package com.team109.javara.domain.vehicle.dto;

import com.team109.javara.domain.image.entity.Image;
import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

@Getter
@Builder
public class WantedVehicleResponseDto {
    private Long wantedVehicleId;
    private String wantedVehicleNumber;
    private String caseNumber;
    private String crimeType;
    private String ownerName;
    private WantedVehicleStatus wantedVehicleStatus;
    private String notes;
    private LocalDateTime createdAt;
    private String deviceId;
    private String imageUrl;
    //위도 경도
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static WantedVehicleResponseDto fromEntity(WantedVehicle vehicle) {
        return WantedVehicleResponseDto.builder()
                .wantedVehicleId(vehicle.getWantedVehicleId())
                .wantedVehicleNumber(vehicle.getWantedVehicleNumber())
                .caseNumber(vehicle.getCaseNumber())
                .crimeType(vehicle.getCrimeType())
                .ownerName(vehicle.getOwnerName())
                .wantedVehicleStatus(vehicle.getWantedVehicleStatus())
                .notes(vehicle.getNotes())
                .createdAt(vehicle.getCreatedAt())
                .deviceId(vehicle.getLocations().stream()
                        .max(Comparator.comparing(WantedVehicleLocation::getReporterName))
                        .map(WantedVehicleLocation::getReporterName)
                        .orElse(null))
                .imageUrl(vehicle.getImages().stream()
                        .max(Comparator.comparing(Image::getCreatedAt))
                        .map(Image::getImageUrl)
                        .orElse(null))
                //가장 최근의 위치값
                .latitude(
                        vehicle.getLocations().stream()
                                .max(Comparator.comparing(WantedVehicleLocation::getSightedAt))
                                .map(WantedVehicleLocation::getLatitude)
                                .orElse(null)
                )
                .longitude(
                        vehicle.getLocations().stream()
                                .max(Comparator.comparing(WantedVehicleLocation::getSightedAt))
                                .map(WantedVehicleLocation::getLongitude)
                                .orElse(null)
                )
                .build();
    }

    public static WantedVehicleResponseDto fromEntityToPolice(WantedVehicle wantedVehicle) {
        return WantedVehicleResponseDto.builder()
                .wantedVehicleId(wantedVehicle.getWantedVehicleId())
                .wantedVehicleNumber(wantedVehicle.getWantedVehicleNumber())
                .caseNumber(wantedVehicle.getCaseNumber())
                .crimeType(wantedVehicle.getCrimeType())
                .ownerName(wantedVehicle.getOwnerName())
                .notes(wantedVehicle.getNotes())
                .wantedVehicleStatus(wantedVehicle.getWantedVehicleStatus())
                .createdAt(wantedVehicle.getCreatedAt())
                //가장 최근의 위치값에 있는 디바이스id 줌
                .deviceId(wantedVehicle.getLocations().stream()
                        .max(Comparator.comparing(WantedVehicleLocation::getReporterName))
                        .map(WantedVehicleLocation::getReporterName)
                        .orElse(null))
                .imageUrl(wantedVehicle.getImages().stream()
                        .max(Comparator.comparing(Image::getCreatedAt))
                        .map(Image::getImageUrl)
                        .orElse(null))
                        //가장 최근의 위치값
                .latitude(
                        wantedVehicle.getLocations().stream()
                                .max(Comparator.comparing(WantedVehicleLocation::getSightedAt))
                                .map(WantedVehicleLocation::getLatitude)
                                .orElse(null)
                )
                .longitude(
                        wantedVehicle.getLocations().stream()
                                .max(Comparator.comparing(WantedVehicleLocation::getSightedAt))
                                .map(WantedVehicleLocation::getLongitude)
                                .orElse(null)
                )
                .build();
    }

    public static WantedVehicleResponseDto fromEntityToUser(WantedVehicle wantedVehicle) {
        return WantedVehicleResponseDto.builder()
                .wantedVehicleId(wantedVehicle.getWantedVehicleId())
                .wantedVehicleNumber(wantedVehicle.getWantedVehicleNumber())
                .caseNumber(null)
                .crimeType(null)
                .ownerName(null)
                .notes(null)
                .wantedVehicleStatus(wantedVehicle.getWantedVehicleStatus())
                .createdAt(wantedVehicle.getCreatedAt())
                //가장 최근의 위치값에 있는 디바이스id
                .deviceId(wantedVehicle.getLocations().stream()
                        .max(Comparator.comparing(WantedVehicleLocation::getReporterName))
                        .map(WantedVehicleLocation::getReporterName)
                        .orElse(null))
                .imageUrl(wantedVehicle.getImages().stream()
                        .max(Comparator.comparing(Image::getCreatedAt))
                        .map(Image::getImageUrl)
                        .orElse(null))
                //가장 최근의 위치값
                .latitude(
                        wantedVehicle.getLocations().stream()
                                .max(Comparator.comparing(WantedVehicleLocation::getSightedAt))
                                .map(WantedVehicleLocation::getLatitude)
                                .orElse(null)
                )
                .longitude(
                        wantedVehicle.getLocations().stream()
                                .max(Comparator.comparing(WantedVehicleLocation::getSightedAt))
                                .map(WantedVehicleLocation::getLongitude)
                                .orElse(null)
                )
                .build();
    }
}
