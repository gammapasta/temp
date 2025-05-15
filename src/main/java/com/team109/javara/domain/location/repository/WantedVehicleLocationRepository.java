package com.team109.javara.domain.location.repository;

import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WantedVehicleLocationRepository extends JpaRepository<WantedVehicleLocation, Long> {

    Optional<WantedVehicleLocation> findTopByWantedVehicle_WantedVehicleIdOrderBySightedAtDesc(Long wantedVehicleId);
    Optional<WantedVehicleLocation> findTopByWantedVehicle_WantedVehicleNumberOrderBySightedAtDesc(String vehicleNumber);

    List<WantedVehicleLocation> findByLatitudeBetweenAndLongitudeBetween(
            double swLat, double neLat, double swLng, double neLng
    );

}

