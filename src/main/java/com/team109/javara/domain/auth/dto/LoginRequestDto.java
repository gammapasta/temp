package com.team109.javara.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto{
    @NotBlank(message = "사용자 아이디는 필수 항목입니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String password;

}