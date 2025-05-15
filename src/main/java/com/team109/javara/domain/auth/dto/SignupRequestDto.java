package com.team109.javara.domain.auth.dto;

import com.team109.javara.domain.member.entity.enums.Gender;
import com.team109.javara.domain.member.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

//추가 필요!!!
@Data
public class SignupRequestDto {
    @NotBlank(message = "사용자 아이디는 필수 항목입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하이어야 합니다.")
    private String username;

    @NotBlank(message = "이름은 필수 항목입니다.")
    @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    //Enum
    private Gender gender;

    private String policeId;

}