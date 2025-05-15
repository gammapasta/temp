package com.team109.javara.domain.member.service;

import com.team109.javara.domain.member.dto.MemberRequestDto;
import com.team109.javara.domain.member.dto.MemberInfoResponseDto;
import com.team109.javara.domain.member.dto.MemberSearchRequestDto;
import com.team109.javara.domain.member.dto.MemberUpdateByFeildRequestDto;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.Gender;
import com.team109.javara.domain.member.entity.enums.Role;
import com.team109.javara.domain.member.repository.MemberRepository;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMemberServiceImpl implements AdminMemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    // Create
    @Override
    public MemberInfoResponseDto createMember(MemberRequestDto requestDto) {

        if (memberRepository.existsByUsername(requestDto.getUsername())) {
            throw new GlobalException(ErrorCode.DUPLICATE_USERNAME, "이미 사용 중인 아이디입니다.");
        }
        Member member = Member.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                .gender(Gender.valueOf(requestDto.getGender()))
                .role(Role.valueOf(requestDto.getRole()))
                .policeId(requestDto.getPoliceId())
                .edgeDeviceId(requestDto.getEdgeDeviceId())
                .build();

        return MemberInfoResponseDto.fromEntity(memberRepository.save(member));
    }

    // Read All
    @Override
    public List<MemberInfoResponseDto> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberInfoResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Read by ID
    @Override
    public MemberInfoResponseDto getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return MemberInfoResponseDto.fromEntity(member);
    }

    // Update
    @Override
    public MemberInfoResponseDto updateMember(Long memberId, MemberRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));


        member.changePassword(passwordEncoder.encode(requestDto.getPassword()));
        member.setMemberStatus(member.getMemberStatus()); // 필요하면 수정
        member.changeRole(Role.valueOf(requestDto.getRole()));
        member.setEdgeDeviceId(requestDto.getEdgeDeviceId());
        // 기타 필요 필드 추가 수정

        return MemberInfoResponseDto.fromEntity(memberRepository.save(member));
    }

    // Delete by ID
    @Override
    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    //search By Feild
    @Override
    public Page<MemberInfoResponseDto> searchMembers(MemberSearchRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());


        Role role = null;
        if (searchRequest.getRole() != null && !searchRequest.getRole().isBlank()) {
            try {
                role = Role.valueOf(searchRequest.getRole().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("유효하지 않은 Role 값입니다: " + searchRequest.getRole());
            }
        }

        Gender gender = null;
        if (searchRequest.getGender() != null && !searchRequest.getGender().isBlank()) {
            try {
                gender = Gender.valueOf(searchRequest.getGender().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("유효하지 않은 Gender 값입니다: " + searchRequest.getGender());
            }
        }

        Page<Member> members = memberRepository.searchByFilters(
                searchRequest.getUsername(),
                searchRequest.getName(),
                searchRequest.getPoliceId(),
                gender,
                role,
                pageable
        );

        return members.map(MemberInfoResponseDto::fromEntity);
    }

    @Override
    public MemberInfoResponseDto updateMemberField(Long memberId, MemberUpdateByFeildRequestDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

        String field = dto.getField();
        String value = dto.getNewValue();

        switch (field) {
            case "name" -> member.setName(value);
            case "password" -> member.setPassword(passwordEncoder.encode(value)); // 비밀번호 수정(뺄까 말까 고민)
            case "gender" -> member.setGender(Gender.valueOf(value));
            case "role" -> member.setRole(Role.valueOf(value));
            case "policeId" -> member.setPoliceId(value);
            case "edgeDeviceId" -> member.setEdgeDeviceId(value);
            default -> throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 필드명입니다.");
        }

        return MemberInfoResponseDto.fromEntity(memberRepository.save(member));


    }
}
