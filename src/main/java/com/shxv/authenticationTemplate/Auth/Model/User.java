package com.shxv.authenticationTemplate.Auth.Model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User {

    @Id
    @Column("id")
    private UUID id;

    @Column("username")
    private String username;

    @Column("password")
    private String password;

    @Column("email")
    private String email;

    @Column("phone_number")
    private String phoneNumber;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("role")
    private UUID role;

    @Column("is_verified")
    private boolean isVerified;

    @Column("account_status")
    private String accountStatus;

    @Column("account_locked")
    private boolean accountLocked;

    @Column("failed_login_attempts")
    private int failedLoginAttempts;

    @Column("last_login_at")
    private LocalDateTime lastLoginAt;

    @Column("logo_path")
    private String logoPath;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
