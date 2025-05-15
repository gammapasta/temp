package com.team109.javara.domain.vehicle.controller;

import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.domain.vehicle.dto.*;
import com.team109.javara.domain.vehicle.service.AppWantedVehicleService;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/wanted-vehicles")
public class AppWantedVehicleController {
    private final MemberRepository memberRepository;
    private final AppWantedVehicleService appWantedVehicleService;
    private final MemberService memberService;

    @Operation(summary = "수배차량 전체 조회", description = "모든 수배차량 정보를 조회합니다.")
    @GetMapping
    public BaseResponse<List<WantedVehicleResponseDto>> getAllWantedVehicles(@AuthenticationPrincipal UserDetails userDetails) {
        Member member = memberService.getCurrentMember(userDetails);
        if(member.getRole() == Role.USER){
            return BaseResponse.fail("접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED);
        }
        List<WantedVehicleResponseDto> vehicles = appWantedVehicleService.getAllWantedVehicles();
        return BaseResponse.success("수배차량 조회 성공", vehicles);
    }

    @Operation(summary = "수배차량 조회", description = "수배차량 정보를 조회합니다.")
    @GetMapping("/{wanted-vehicle}")
    public BaseResponse<WantedVehicleResponseDto> getWantedVehicles(@PathVariable("wanted-vehicle") String wantedVehicleNumber, @AuthenticationPrincipal UserDetails userDetails) {
        Member member = memberService.getCurrentMember(userDetails);
        if(member.getRole() == Role.USER){
            return BaseResponse.fail("접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED);
        }
        WantedVehicleResponseDto vehicle = appWantedVehicleService.getWantedVehicle(wantedVehicleNumber);
        return BaseResponse.success("수배차량 조회 성공", vehicle);
    }


    @Operation(summary = "내가 발견한 수배차량 목록 조회", description = "내가 발견한 수배차량 목록 조회합니다.")
    @GetMapping("/detections")
    public BaseResponse<List<WantedVehicleResponseDto>> getDetections(@AuthenticationPrincipal UserDetails userDetails) {
        //로그인 정보로  member 가져옴
        Member member = memberService.getCurrentMember(userDetails);
        //끝

        List<WantedVehicleResponseDto> vehicle = appWantedVehicleService.getDetection(member.getReporterName(), member.getRole());
        log.info("member reporter name {}",member.getReporterName());
        return BaseResponse.success("수배차량 조회 성공", vehicle);
    }
}
