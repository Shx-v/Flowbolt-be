package com.shxv.authenticationTemplate.ProjectMember.DTO;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionResponse {

    private String key;
    private String description;

}
