package com.team109.javara.domain.member.controller;

import com.team109.javara.domain.member.dto.MyInfoDto;
import com.team109.javara.domain.member.dto.MemberPenaltyPointsDto;
import com.team109.javara.domain.member.dto.MemberUpdatePasswordDto;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.domain.member.service.MemberService;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import com.team109.javara.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/members")
public class AppMemberController {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;



    @Operation(summary = "비밀번호 변경 post")
    @PostMapping(value = "/password")
    public BaseResponse<Object> updatePassword(
            @Valid @RequestBody MemberUpdatePasswordDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        //로그인 정보로  member 가져옴
        Member member = memberService.getCurrentMember(userDetails);
        //끝

        //현재 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())) {
            throw new GlobalException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        //현재비번과 바꿀비번 같은지
        if(requestDto.getCurrentPassword().equals(requestDto.getNewPassword())) {
            throw new GlobalException(ErrorCode.DUPLICATE_PASSWORD);
        }

        String encodedNewPassword = passwordEncoder.encode(requestDto.getNewPassword());

        try {
            member.changePassword(encodedNewPassword);
            memberRepository.save(member);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "입력값을 다시 확인하세요");
        }

        return BaseResponse.success("비밀번호 변경에 성공했습니다");
    }
    @Operation(summary = "엣지디바이스 변경")
    @PostMapping(value = "/device/{deviceId}")
    public BaseResponse<Object> updateEdgeDeviceId(@PathVariable String deviceId, @AuthenticationPrincipal UserDetails userDetails) {
        //로그인 정보로  member 가져옴
        Member member = memberService.getCurrentMember(userDetails);
        //끝

        memberService.updateEdgeDeviceId(member, deviceId);
        return BaseResponse.success("엣지디바이스 변경에 성공했습니다.", deviceId);
    }

    @GetMapping("/penalty")
    public BaseResponse<MemberPenaltyPointsDto> getPenaltyPoints(@AuthenticationPrincipal UserDetails userDetails) {
        //로그인 정보로  member 가져옴
        Member member = memberService.getCurrentMember(userDetails);

        MemberPenaltyPointsDto response = memberService.getPenaltyPoints(member);
        return BaseResponse.success("벌점 조회 성공", response);
    }

    @GetMapping("/me")
    public BaseResponse<MyInfoDto> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        Member member = memberService.getCurrentMember(userDetails);
        MyInfoDto myInfoDto = MyInfoDto.fromEntity(member);
        return BaseResponse.success("내 정보 조회 성공.", myInfoDto);
    }
}
