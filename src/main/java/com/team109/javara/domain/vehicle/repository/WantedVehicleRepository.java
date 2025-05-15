package com.team109.javara.domain.vehicle.repository;

import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WantedVehicleRepository extends JpaRepository<WantedVehicle, Long> {
    List<WantedVehicle> findAllByOrderByCreatedAtDesc();
    Optional<WantedVehicle> findByWantedVehicleNumber(String wantedVehicleNumber);
    boolean existsByCaseNumber(String caseNumber);

    //경찰의 경우
    List<WantedVehicle> findByTasks_AssignedMember_Id(Long memberId);
    //해당 사용자가 발견한 수배차량중 특정 status
    List<WantedVehicle> findByTasks_AssignedMember_IdAndWantedVehicleStatus(Long memberId, WantedVehicleStatus status);

    //일반 사용자 경우
    List<WantedVehicle> findByLocations_ReporterName(String reporterName);

    boolean existsByCaseNumberAndWantedVehicleIdNot(String caseNumber, Long wantedVehicleId);


    //특정 조건으로 검색 쿼리
    @Query("SELECT w FROM WantedVehicle w WHERE " +
            "(:wantedVehicleNumber IS NULL OR w.wantedVehicleNumber LIKE %:wantedVehicleNumber%) AND " +
            "(:caseNumber IS NULL OR w.caseNumber LIKE %:caseNumber%) AND " +
            "(:crimeType IS NULL OR w.crimeType LIKE %:crimeType%) AND " +
            "(:ownerName IS NULL OR w.ownerName LIKE %:ownerName%) AND " +
            "(:wantedVehicleStatus IS NULL OR w.wantedVehicleStatus = :wantedVehicleStatus) AND " +
            "(:deviceId IS NULL OR EXISTS (SELECT loc FROM w.locations loc WHERE loc.reporterName = :deviceId))")
    Page<WantedVehicle> searchByFilters(@Param("wantedVehicleNumber") String wantedVehicleNumber,
                                        @Param("caseNumber") String caseNumber,
                                        @Param("crimeType") String crimeType,
                                        @Param("ownerName") String ownerName,
                                        @Param("wantedVehicleStatus") WantedVehicleStatus wantedVehicleStatus,
                                        @Param("deviceId") String deviceId,
                                        Pageable pageable);

}
