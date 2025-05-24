package com.team109.javara.domain.member.dto;

import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.member.entity.enums.Gender;
import com.team109.javara.domain.member.entity.enums.MemberStatus;
import com.team109.javara.domain.member.entity.enums.Role;
import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MyInfoDto {

    private Long id;
    private String username;
    private String name;
    private Gender gender;
    private Role role;
    private String policeId;
    private MemberStatus memberStatus; //순찰상태
    private Double penaltyPoints;
    private LocalDateTime createdAt;
    private String edgeDeviceId;

    public static MyInfoDto fromEntity(Member member) {
        return MyInfoDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .name(member.getName())
                .gender(member.getGender())
                .role(member.getRole())
                .policeId(member.getPoliceId())
                .memberStatus(member.getMemberStatus())
                .penaltyPoints(member.getPenaltyPoints())
                .createdAt(member.getCreatedAt())
                .edgeDeviceId(member.getEdgeDeviceId())
                .build();
    }
}
