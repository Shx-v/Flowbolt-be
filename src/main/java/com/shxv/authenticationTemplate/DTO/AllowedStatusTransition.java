package com.shxv.authenticationTemplate.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllowedStatusTransition {
    private EnumOptionDTO toStatus;
    private List<String> requiredPermissions;
}
