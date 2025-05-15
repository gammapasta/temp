package com.team109.javara.domain.member.dto;

import com.team109.javara.domain.member.entity.enums.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchRequestDto {

    private String username;
    private String name;
    private String policeId;
    private String gender;
    private String role;

    private int page = 0;
    private int size = 10;
}
