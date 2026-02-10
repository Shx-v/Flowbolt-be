package com.shxv.authenticationTemplate.Security.Jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal {
    private UUID userId;
    private String username;
    private UUID roleId;
    private List<String> permissions;
}

