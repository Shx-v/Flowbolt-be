package com.shxv.flowbolt.Project.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProjectCreate {

    private String name;
    private String projectCode;
    private String description;
    private UUID owner;

}
