package com.shxv.authenticationTemplate.Auth.DTO;

import java.util.List;
import java.util.UUID;

public record UserJwtDTO(
        UUID id,
        String username,
        UUID roleId
) {}

