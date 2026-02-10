package com.shxv.authenticationTemplate.Role.DTO;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest {
    private String name;
    private String description;
    private List<String> globalPermissionKeys;
}
