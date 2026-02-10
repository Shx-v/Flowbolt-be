package com.shxv.authenticationTemplate.Auth.Model;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Table("sessions")
@Accessors(chain = true)
public class Session {

    @Id
    private UUID id;

    private UUID userId;

    private String accessToken;

    private String refreshToken;

    private Instant issuedAt;

    private Instant expiresAt;

    private boolean active;
}

