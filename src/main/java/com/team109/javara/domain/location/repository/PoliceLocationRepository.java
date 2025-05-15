package com.team109.javara.domain.location.repository;

import com.team109.javara.domain.location.entity.PoliceLocation;
import com.team109.javara.domain.member.entity.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PoliceLocationRepository extends JpaRepository<PoliceLocation, Long> {
    // 특정 멤버의 모든 위치 기록 조회 (최신순)
    List<PoliceLocation> findByMemberIdOrderByCreatedAtDesc(Member member);

    // 특정 멤버의 가장 최신 위치 기록 하나만 조회 (JPA 쿼리 메서드 키워드 활용)
    Optional<PoliceLocation> findTopByMemberIdOrderByCreatedAtDesc(Member member);

    // 특정 기간(createdAt) 안의 기록
    List<PoliceLocation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 주어진 위치의 박스 범위 계산
        @Query("""
        SELECT pl
        FROM PoliceLocation pl
        WHERE pl.member.id NOT IN :excludedIds
          AND pl.latitude BETWEEN :minLat AND :maxLat
          AND pl.longitude BETWEEN :minLng AND :maxLng
        """)
        List<PoliceLocation> findWithinBoxExcluding(
                @Param("excludedIds") Set<Long> excludedIds,
                @Param("minLat") double minLat,
                @Param("maxLat") double maxLat,
                @Param("minLng") double minLng,
                @Param("maxLng") double maxLng
        );

}