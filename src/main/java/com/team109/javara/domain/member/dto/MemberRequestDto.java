package com.team109.javara.domain.member.dto;

import com.team109.javara.domain.member.entity.enums.Gender;
import com.team109.javara.domain.member.entity.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MemberRequestDto {
    private String username;    // 로그인 ID
    private String password;    // 비밀번호 (암호화 저장)
    private String name;        // 이름
    private Gender gender;      // MALE / FEMALE (문자열로 받음)
    private Role role;        // ADMIN / POLICE / USER (문자열로 받음)
    private Double penaltyPoints;
    private String policeId;    // 경찰 코드
    private String edgeDeviceId;// 엣지디바이스 ID
}
