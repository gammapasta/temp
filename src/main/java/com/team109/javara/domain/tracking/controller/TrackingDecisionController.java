package com.team109.javara.domain.tracking.controller;

import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.domain.tracking.dto.TrackingDecisionRequestDto;
import com.team109.javara.domain.tracking.dto.TrackingResultRequestDto;
import com.team109.javara.domain.tracking.service.TrackingDecisionService;
import com.team109.javara.domain.vehicle.dto.ArrestStatusDto;
import com.team109.javara.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/decision")
public class TrackingDecisionController {
    private final MemberService memberService;
    private final TrackingDecisionService trackingDecisionService;

    @Operation(summary = "추적 수락 여부 결정", description = "추적 수락 여부 경정.")
    @PostMapping("/tracking")
    public BaseResponse<Object> submitTrackingDecision(@Valid @RequestBody TrackingDecisionRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        memberService.verifyCurrentMember(userDetails);
        trackingDecisionService.handleTrackingDecision(requestDto);
        return BaseResponse.success("추적 수락 여부 결정 전송 성공");
    }

    @Operation(summary = "완료 여부 결정", description = "완료 여부 결정.")
    @PostMapping("/result")
    public BaseResponse<Object> submitResult(@Valid @RequestBody TrackingResultRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        memberService.verifyCurrentMember(userDetails);
        trackingDecisionService.handleResultDecision(requestDto);

        return BaseResponse.success("추적 수락 여부 결정 전송 성공");
    }

}
