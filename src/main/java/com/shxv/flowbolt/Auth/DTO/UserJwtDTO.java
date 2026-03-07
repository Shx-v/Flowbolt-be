package com.shxv.flowbolt.Auth.DTO;

import java.util.List;
import java.util.UUID;

public record UserJwtDTO(
        UUID id,
        String username,
        UUID roleId
) {}

