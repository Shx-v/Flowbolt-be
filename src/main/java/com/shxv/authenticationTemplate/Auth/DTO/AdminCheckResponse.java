package com.shxv.authenticationTemplate.Auth.DTO;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AdminCheckResponse {
    private Boolean isAdmin;
    private UUID userId;
}
