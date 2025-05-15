package com.team109.javara.domain.edgeDevice.controller;

import com.team109.javara.domain.edgeDevice.dto.VerificationResponseDto;
import com.team109.javara.domain.image.dto.ImageResponse;
import com.team109.javara.domain.image.entity.Image;
import com.team109.javara.domain.image.service.ImageService;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.domain.tracking.service.TrackingDecisionService;
import com.team109.javara.domain.vehicle.dto.EdgeDeviceWantedVehicleResponseDto;
import com.team109.javara.domain.vehicle.dto.WantedVehicleResponseDto;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.domain.vehicle.entity.enums.WantedVehicleStatus;
import com.team109.javara.domain.vehicle.repository.WantedVehicleRepository;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import com.team109.javara.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/edge-devices")
public class EdgeDeviceController {
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final ImageService imageService;
    private final WantedVehicleRepository wantedVehicleRepository;
    private final TrackingDecisionService trackingDecisionService;
    // edgeDevice <-> server
    //TODO 검증 밑, 내장 db 업데이트
    @Operation(summary = "[엣지디바이스 전용] 수배차량 검증")
    @PostMapping(value = "/verification")
    public BaseResponse<VerificationResponseDto> verify(@RequestParam("file") MultipartFile file,
                                                        @RequestParam(value = "deviceId", required = true) String deviceId,
                                                        @RequestParam(value = "wantedVehicleNumber", required = true) String wantedVehicleNumber
    ){
        Member member = memberRepository.findByEdgeDeviceId(deviceId).orElseThrow(()-> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
        WantedVehicle wantedVehicle = wantedVehicleRepository.findByWantedVehicleNumber(wantedVehicleNumber).orElseThrow(()-> new GlobalException(ErrorCode.VEHICLE_NOT_FOUND));

        if(wantedVehicle.getWantedVehicleStatus() != WantedVehicleStatus.WANTED){
            return BaseResponse.fail("수배중이 아닙니다", HttpStatus.CONFLICT);
        }

        try{
            imageService.saveImage(file, member, wantedVehicle);
        }catch (Exception e){
            log.warn("엣지 디바이스로부터 받은 파일지 존제하지 않습니다. deviceId [{}]", deviceId, e);
        }
        log.info("엣지디바이스 [{}]에서 보내온 이미지 저장 성공",deviceId);

        VerificationResponseDto responseDto = new VerificationResponseDto();
        responseDto.setWantedVehicleNumber(wantedVehicle.getWantedVehicleNumber());
        responseDto.setWantedVehicleStatus(wantedVehicle.getWantedVehicleStatus());

        //경찰이면서 active 경우 알림 보내야함. 아니면 그냥 일반 시민같이 수배차량 위치만 전송
        if(member.getRole() == Role.POLICE && member.getMemberStatus() == MemberStatus.ACTIVE){
            try {
                trackingDecisionService.initiateFirstTaskDecision(member.getEdgeDeviceId(), wantedVehicle.getWantedVehicleNumber());
                log.info("initiateFirstTaskDecision 처리 완료");
            }catch (Exception e){
                return BaseResponse.fail("Task 상태가 ACTIVE 일떄만 가능합니다.", HttpStatus.BAD_REQUEST);
            }
        }

        return BaseResponse.success("수배차량입니다", responseDto);
    }


    @Operation(summary = "[엣지디바이스 전용] 수배차량목록 가져오기")
    @GetMapping("/WantedVehicleList")
    public BaseResponse<List<EdgeDeviceWantedVehicleResponseDto>> getWantedVehicleList(){
        List<EdgeDeviceWantedVehicleResponseDto> responses =wantedVehicleRepository.findAll().stream()
                .map(EdgeDeviceWantedVehicleResponseDto::fromEntity)
                .collect(Collectors.toList());
        return BaseResponse.success("전체 수배 차량 조회 성공", responses);
    }





    // app <-> server
    @Operation(summary = "경찰 순찰모드", description = "경찰 순찰모드입니다.")
    @PostMapping(value = "/app/police/{status}")
    public BaseResponse<MemberStatus> patrolMode(@PathVariable String status, @AuthenticationPrincipal UserDetails userDetails) {
        Member member = memberService.getCurrentMemberPolice(userDetails);

        // status 대문자로 바꿔줌
        MemberStatus memberStatus = MemberStatus.from(status);

        try{
            log.info("id {} memberStatus {} ", member.getId(), memberStatus);
            memberService.updateMemberStatus(member, memberStatus);
            log.info("updatePoliceStatus");
        }catch (NullPointerException e){
            throw new GlobalException(ErrorCode.DEVICE_NOT_FOUND);
        }

        return BaseResponse.success("순찰모드 변경을 성공했습니다", memberStatus);
    }

    @Operation(summary = "일반 시민 주행모드", description = "일반 시민 주행모드입니다.")
    @PostMapping(value = "/app/user/{status}")
    public BaseResponse<MemberStatus> driveMode(@PathVariable String status, @AuthenticationPrincipal UserDetails userDetails) {
        //로그인 정보로  member 가져옴
        Member member = memberService.getCurrentMember(userDetails);
        //끝

        // status 대문자로 바꿔줌
        MemberStatus memberStatus = MemberStatus.from(status);


        try{
            log.info("id {} memberStatus {} ", member.getId(), memberStatus);
            memberService.updateMemberStatus(member, memberStatus);
            log.info("updateMemberStatus");
        }catch (NullPointerException e){
            throw new GlobalException(ErrorCode.DEVICE_NOT_FOUND);
        }

        return BaseResponse.success("주행모드 변경을 성공했습니다.", memberStatus);
    }

}