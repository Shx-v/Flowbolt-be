package com.shxv.authenticationTemplate.Group.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GroupListResponse {
    private UUID id;
    private String name;
}
