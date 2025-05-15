package com.team109.javara.domain.location.service;

import com.team109.javara.domain.location.dto.PoliceLocationRequestDto;
import com.team109.javara.domain.location.dto.PoliceLocationResponseDto;
import com.team109.javara.domain.location.entity.PoliceLocation;
import com.team109.javara.domain.location.repository.PoliceLocationRepository;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.task.entity.Task;
import com.team109.javara.domain.task.repository.TaskRepository;
import com.team109.javara.domain.webSocket.service.WebSocketService;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PoliceLocationServiceImpl implements PoliceLocationService {

    private final PoliceLocationRepository policeLocationRepository;
    private final MemberRepository memberRepository;

    //테스트 데이터 생성용 Create
    @Override
    public PoliceLocationResponseDto createLocation(PoliceLocationRequestDto requestDto) {
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        PoliceLocation newLocation = new PoliceLocation();
        newLocation.setMember(member);
        newLocation.setLatitude(requestDto.getLatitude());
        newLocation.setLongitude(requestDto.getLongitude());

        PoliceLocation saved = policeLocationRepository.save(newLocation);

        return PoliceLocationResponseDto.fromEntity(saved);
    }

    //모든 경찰 위치정보 조회
    @Override
    public List<PoliceLocationResponseDto> getAllLocations() {
        return policeLocationRepository.findAll().stream()
                .map(PoliceLocationResponseDto::fromEntity)
                .toList();
    }

    //아이디로 경찰 위치정보 조회
    @Override
    public PoliceLocationResponseDto getLocationById(Long id) {
        PoliceLocation location = policeLocationRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ErrorCode.RESOURCE_NOT_FOUND));
        return PoliceLocationResponseDto.fromEntity(location);
    }

    //멤버아이디로 경찰 위치정보 조회
    @Override
    public List<PoliceLocationResponseDto> getLocationsByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        return policeLocationRepository.findByMemberIdOrderByCreatedAtDesc(member).stream()
                .map(PoliceLocationResponseDto::fromEntity)
                .toList();
    }

    //특정 날짜에 생성된 위치정보 조회
    @Override
    public List<PoliceLocationResponseDto> getLocationsByCreatedAtBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return policeLocationRepository.findByCreatedAtBetween(start, end).stream()
                .map(PoliceLocationResponseDto::fromEntity)
                .toList();
    }

}

