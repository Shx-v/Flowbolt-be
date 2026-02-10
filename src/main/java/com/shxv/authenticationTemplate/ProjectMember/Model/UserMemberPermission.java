package com.shxv.authenticationTemplate.ProjectMember.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("project_user_member_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMemberPermission {

    @Id
    @Column("id")
    private UUID id;

    @Column("project_user_member_id")
    private UUID projectUserMemberId;

    @Column("permission_id")
    private UUID permissionId;

    @Column("active")
    private Boolean active;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
