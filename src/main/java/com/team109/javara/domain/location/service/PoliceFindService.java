package com.team109.javara.domain.location.service;

import com.team109.javara.domain.location.entity.PoliceLocation;
import com.team109.javara.domain.location.entity.WantedVehicleLocation;
import com.team109.javara.domain.location.repository.PoliceLocationRepository;
import com.team109.javara.domain.location.repository.WantedVehicleLocationRepository;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PoliceFindService {

    private final PoliceLocationRepository policeLocationRepository;
    private final MemberRepository memberRepository;
    private final WantedVehicleLocationRepository wantedVehicleLocationRepository;

    /**
     * Task의 수배차량 최신 위치 기준으로, 반경 내 가장 가까운 경찰 찾기
     *
     * @param task 현재 Task 정보
     * @return 새로 할당할 Member (없으면 null)
     */
    public Member findNextAvailablePolice(Task task) {

        // 1. task 엔티티 -> wantedVehicle 엔티티 -> 해당 수배차량 id 추출
        // ** wantedVehicleId ** <<-- 탐색된 수배차량 ID
        Long wantedVehicleId = task.getWantedVehicle().getWantedVehicleId();

        // 2. 수배차량의 최신 위치 가져오기
        // ** lastestLocation ** <<-- 탐색된 수배차량 ID의 최근 위치
        WantedVehicleLocation latestLocation = wantedVehicleLocationRepository
                .findTopByWantedVehicle_WantedVehicleIdOrderBySightedAtDesc(wantedVehicleId)
                // ex: 예외 처리(해당 wantedVehicleId로 된 위치정보가 없을 때.
                .orElseThrow(() -> new GlobalException(ErrorCode.LOCATION_NOT_FOUND));

        // 3. 해당 수배차량의 위치를 중심 좌표로 설정 (centerLat - 위도, centerLng - 경도)
        // BigDecimal 타입을 double 타입으로 바꿈 (계산을 위해)
        double centerLat = latestLocation.getLatitude().doubleValue();
        double centerLng = latestLocation.getLongitude().doubleValue();

        // 4. 거절한 경찰의 Id 목록 (해당 경찰은 제외하고 탐색하기 위해)
        Set<Long> rejectedIds = new HashSet<>();
        if (task.getAssignedMember() != null) { // Task의 assignMember 필드 확인
            rejectedIds.add(task.getAssignedMember().getId()); // 해당 필드에 있는 memberId를 거절한 경찰의 Id 목록에 추가.
        }

        // 5. 탐색 반경 단계 (.1km → .5km → 1km → 5km → 10km) , TODO: 조정 가능.
        double[] searchRadii = {0.1, 0.5, 1, 5, 10};

        //6. 탐색 로직 (for 문으로 searchRadii를 늘리면서 조회)
        for (double radius : searchRadii) {
            // 7. 박스범위 계산식
            double delta = radius / 111.0; // radius = 10, 20, 30 ... 50  (위도 1도 ≈ 111km) # delta = x.xx도 (km 아님!)
            double minLat = centerLat - delta; // 박스범위 최소 위도
            double maxLat = centerLat + delta; // 박스범위 최대 위도
            double minLng = centerLng - delta; // 박스범위 최소 경도
            double maxLng = centerLng + delta; // 박스범위 최대 경도

            // 8. 박스 범위 내 후보군 조회 (이미 reject한 경찰 제외)
            // ** candidates ** <<-- 박스 범위 안의 경찰 후보군
            List<PoliceLocation> candidates = policeLocationRepository.findWithinBoxExcluding(
                    rejectedIds, minLat, maxLat, minLng, maxLng //거절한 경찰 ID, 박스 범위 좌표
            );
            // 만약, 해당 반경에 경찰이 없다면..
            if (candidates.isEmpty()) {
                continue; // 다음 반경으로 (6번으로 돌아감)
            }

            // 9. 후보 중 가장 가까운 경찰 선택 (하버사인 거리 계산)
            // ** closest ** <<-- 가까운 경찰
            Optional<PoliceLocation> closest = candidates.stream() // candidates(탐색 후보군)을 stream 형식으로 변환
                    //min(각 후보 경찰과 중심위치(수배차량) 의 거리) => 최소값 추출
                    .min(Comparator.comparingDouble(candidate ->
                            // 하버사인공식 계산
                            calculateHaversine(centerLat, centerLng, // 수배차량의 위도, 경도
                                    candidate.getLatitude().doubleValue(), // 후보 경찰의 위도
                                    candidate.getLongitude().doubleValue() // 후보 경찰의 경도
                            )));

            // 찾으면 ->
            if (closest.isPresent()) {
                log.info("가장 가까운 경찰 찾음: 경찰 ID {}", closest.get().getMember().getId());
                return closest.get().getMember(); // 가장 가까운 경찰(Member) 엔티티 반환
            }
        }
        // 못찾으면 ->
        log.warn("반경 10km 내에서 적절한 경찰을 찾지 못함.");
        return null; // 못 찾으면 null TODO: 호출부에서 null 처리 필요!!
    }


    // 하버사인 공식: 두 좌표 간 거리(km) 계산, 지구 곡률 반영
    private double calculateHaversine(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS = 6371; // km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLng / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
