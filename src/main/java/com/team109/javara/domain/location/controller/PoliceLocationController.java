package com.team109.javara.domain.location.controller;

import com.team109.javara.domain.location.dto.PLResponseDto;
import com.team109.javara.domain.location.dto.PoliceLocationRequestDto;
import com.team109.javara.domain.location.dto.PoliceLocationResponseDto;
import com.team109.javara.domain.location.service.PoliceLocationService;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/location/police")
@RequiredArgsConstructor
public class PoliceLocationController {

    private final PoliceLocationService policeLocationService;
    private final MemberService memberService;

    @Operation(summary = "police location 위치 조회")
    @GetMapping("/me")
    public BaseResponse<PLResponseDto> getMyLocation(@AuthenticationPrincipal UserDetails userDetails) {
        Member member = memberService.getCurrentMember(userDetails);
        PLResponseDto responseDto = PLResponseDto.fromEntity(member);
        return BaseResponse.success("내 위치(police location) 조회", responseDto);
    }
    @PostMapping
    public BaseResponse<PoliceLocationResponseDto> createLocation(
            @RequestBody PoliceLocationRequestDto requestDto) {
        PoliceLocationResponseDto response = policeLocationService.createLocation(requestDto);
        return BaseResponse.success("생성 성공", response);
    }

    @GetMapping
    public BaseResponse<List<PoliceLocationResponseDto>> getAllLocations() {
        List<PoliceLocationResponseDto> response = policeLocationService.getAllLocations();
        return BaseResponse.success("전체 조회 성공", response);
    }

    @GetMapping("/{id}")
    public BaseResponse<PoliceLocationResponseDto> getLocationById(@PathVariable Long id) {
        PoliceLocationResponseDto response = policeLocationService.getLocationById(id);
        return BaseResponse.success("단일 조회 성공", response);
    }

    @GetMapping("/member/{memberId}")
    public BaseResponse<List<PoliceLocationResponseDto>> getLocationsByMemberId(@PathVariable Long memberId) {
        List<PoliceLocationResponseDto> response = policeLocationService.getLocationsByMemberId(memberId);
        return BaseResponse.success("멤버별 조회 성공", response);
    }

    @GetMapping("/date")
    public BaseResponse<List<PoliceLocationResponseDto>> getLocationsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<PoliceLocationResponseDto> response =
                policeLocationService.getLocationsByCreatedAtBetween(startDate, endDate);

        return BaseResponse.success("기간별 조회 성공", response);
    }
}
