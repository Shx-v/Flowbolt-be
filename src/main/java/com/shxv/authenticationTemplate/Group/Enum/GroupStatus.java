package com.shxv.authenticationTemplate.Group.Enum;

import lombok.Getter;

@Getter
public enum GroupStatus {
    ACTIVE("Active"),
    ARCHIVED("Archived");

    private final String label;

    private GroupStatus(String label) {
        this.label = label;
    }
}
