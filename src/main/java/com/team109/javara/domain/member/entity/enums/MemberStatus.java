package com.team109.javara.domain.member.entity.enums;

import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    NOT_AVAILABLE("Not Available"),
    TRACKING("Tracking"),
    USER("User")
    ;

    private final String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public static MemberStatus from(String value) {
        try {
            return MemberStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "status enum에 없음: " + value);
        }
    }
}