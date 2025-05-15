package com.team109.javara.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateByFeildRequestDto {
    private String field;
    private String newValue;
}
