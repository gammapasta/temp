package com.team109.javara.domain.member.controller;

import com.team109.javara.domain.member.dto.MemberRequestDto;
import com.team109.javara.domain.member.dto.MemberSearchRequestDto;
import com.team109.javara.domain.member.dto.MemberInfoResponseDto;
import com.team109.javara.domain.member.dto.MemberUpdateByFeildRequestDto;
import com.team109.javara.domain.member.service.AdminMemberService;
import com.team109.javara.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @PostMapping
    public BaseResponse<MemberInfoResponseDto> createMember(@RequestBody MemberRequestDto requestDto) {
        MemberInfoResponseDto response = adminMemberService.createMember(requestDto);
        return BaseResponse.success("회원 등록 성공", response);
    }

    @GetMapping
    public BaseResponse<List<MemberInfoResponseDto>> getAllMembers() {
        List<MemberInfoResponseDto> members = adminMemberService.getAllMembers();
        return BaseResponse.success("전체 회원 조회 성공", members);
    }

    @GetMapping("/{id}")
    public BaseResponse<MemberInfoResponseDto> getMemberById(@PathVariable Long id) {
        MemberInfoResponseDto response = adminMemberService.getMemberById(id);
        return BaseResponse.success("회원 조회 성공", response);
    }

    @PutMapping("/{id}")
    public BaseResponse<MemberInfoResponseDto> updateMember(
            @PathVariable Long id,
            @RequestBody MemberRequestDto requestDto) {
        MemberInfoResponseDto response = adminMemberService.updateMember(id, requestDto);
        return BaseResponse.success("회원 수정 성공", response);
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteMember(@PathVariable Long id) {
        adminMemberService.deleteMember(id);
        return BaseResponse.success("회원 삭제 성공", null);
    }

    //검색용
    @PostMapping("/search")
    public BaseResponse<Page<MemberInfoResponseDto>> searchMembers(
            @RequestBody MemberSearchRequestDto searchRequest) {
        Page<MemberInfoResponseDto> result = adminMemberService.searchMembers(searchRequest);
        return BaseResponse.success("회원 검색 성공", result);
    }

    //필드별 수정
    @PutMapping("/{id}/field")
    public BaseResponse<MemberInfoResponseDto> updateMemberField(
            @PathVariable Long id,
            @RequestBody MemberUpdateByFeildRequestDto dto) {
        MemberInfoResponseDto response = adminMemberService.updateMemberField(id, dto);
        return BaseResponse.success("회원 필드 수정 성공", response);
    }
}
