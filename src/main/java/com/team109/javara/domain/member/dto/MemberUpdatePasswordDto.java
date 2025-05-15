package com.team109.javara.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdatePasswordDto {
    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String currentPassword;

    @NotBlank(message = "새로운 비밀번호는 필수 항목입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String newPassword;
}
