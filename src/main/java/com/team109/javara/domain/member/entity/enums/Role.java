package com.team109.javara.domain.member.entity.enums;

public enum Role {
    ADMIN("Admin"),
    POLICE("Police"),
    USER("User");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}