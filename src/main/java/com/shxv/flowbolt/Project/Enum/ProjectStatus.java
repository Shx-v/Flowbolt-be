package com.shxv.flowbolt.Project.Enum;

import lombok.Getter;

@Getter
public enum ProjectStatus {

    ACTIVE("Active"),
    ARCHIVED("Archived"),
    SUSPENDED("Suspended");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }
}
