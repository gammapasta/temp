package com.team109.javara.domain.member.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("Male"),
    FEMALE("Female")
    ;

    private final String displayName;

    public String getDisplayName() {
        return displayName;
    }

}
