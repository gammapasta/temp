package com.team109.javara.domain.member.service;

import com.team109.javara.domain.member.dto.MemberInfoResponseDto;
import com.team109.javara.domain.member.dto.MemberRequestDto;
import com.team109.javara.domain.member.dto.MemberSearchRequestDto;
import com.team109.javara.domain.member.dto.MemberUpdateByFeildRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminMemberService {
    MemberInfoResponseDto createMember(MemberRequestDto requestDto);
    List<MemberInfoResponseDto> getAllMembers();
    MemberInfoResponseDto getMemberById(Long memberId);
    MemberInfoResponseDto updateMember(Long memberId, MemberRequestDto requestDto);
    void deleteMember(Long memberId);
    public Page<MemberInfoResponseDto> searchMembers(MemberSearchRequestDto searchRequest);
    MemberInfoResponseDto updateMemberField(Long memberId, MemberUpdateByFeildRequestDto dto);
}
