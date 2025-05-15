package com.team109.javara.domain.location.service;

import com.team109.javara.domain.location.dto.WantedVehicleLocationResponseDto;
import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import com.team109.javara.domain.location.repository.WantedVehicleLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WantedVehicleLocationService {

    private final WantedVehicleLocationRepository locationRepository;

    public List<WantedVehicleLocationResponseDto> findVehiclesInViewport(double centerLat, double centerLng, int zoomLevel) {
        ViewportCalculator.Bounds bounds = ViewportCalculator.calculateBounds(centerLat, centerLng, zoomLevel);

        List<WantedVehicleLocation> locations = locationRepository.findByLatitudeBetweenAndLongitudeBetween(
                bounds.swLat, bounds.neLat,
                bounds.swLng, bounds.neLng
        );

        // 차량 번호별로 sightedAt 기준 가장 최신 데이터만 유지
        Map<String, WantedVehicleLocation> latestByVehicle = locations.stream()
                .collect(Collectors.toMap(
                        loc -> loc.getWantedVehicle().getWantedVehicleNumber(),   // 차량 번호
                        loc -> loc,                                               // 위치 엔티티
                        (existing, candidate) -> {
                            return candidate.getSightedAt().isAfter(existing.getSightedAt())
                                    ? candidate : existing;
                        }
                ));

        return latestByVehicle.values().stream()
                .map(WantedVehicleLocationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 내부 static 유틸 클래스
    public static class ViewportCalculator {

        public static class Bounds {
            public double neLat;
            public double neLng;
            public double swLat;
            public double swLng;
        }

        public static Bounds calculateBounds(double centerLat, double centerLng, int zoomLevel) {
            Bounds bounds = new Bounds();

            double visibleRadiusKm = getVisibleRadiusKm(zoomLevel);

            double deltaLat = visibleRadiusKm / 111.0;
            double deltaLng = visibleRadiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)));

            bounds.neLat = centerLat + deltaLat;
            bounds.neLng = centerLng + deltaLng;
            bounds.swLat = centerLat - deltaLat;
            bounds.swLng = centerLng - deltaLng;

            return bounds;
        }

        private static double getVisibleRadiusKm(int zoomLevel) {
            return switch (zoomLevel) {
                case 18 -> 0.2;
                case 17 -> 0.5;
                case 16 -> 1.0;
                case 15 -> 2.0;
                case 14 -> 4.0;
                case 13 -> 8.0;
                case 12 -> 16.0;
                case 11 -> 32.0;
                case 10 -> 64.0;
                default -> 100.0;
            };
        }
    }

    public WantedVehicleLocationResponseDto findVehiclesByVehicleNumber(String vehicleNumber) {
        WantedVehicleLocation location = locationRepository
                .findTopByWantedVehicle_WantedVehicleNumberOrderBySightedAtDesc(vehicleNumber)
                .orElseThrow( () -> new RuntimeException("해당 차량의 위치가 없습니다"));

        return WantedVehicleLocationResponseDto.fromEntity(location);
    }
}
