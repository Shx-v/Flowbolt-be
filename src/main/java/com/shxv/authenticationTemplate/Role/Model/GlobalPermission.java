package com.shxv.authenticationTemplate.Role.Model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("global_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalPermission {

    @Id
    private UUID id;

    @Column("key")
    private String key;

    @Column("description")
    private String description;

    @Column("active")
    private Boolean active;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
