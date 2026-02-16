package com.shxv.authenticationTemplate.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCreateRequestDTO {

    private String name;
    private String projectCode;
    private String description;
    private UUID owner;
}
