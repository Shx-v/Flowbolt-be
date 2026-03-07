package com.shxv.flowbolt.Role.Model;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("role_global_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleGlobalPermission {

    @Column("role_id")
    private UUID roleId;

    @Column("permission_id")
    private UUID permissionId;
}
