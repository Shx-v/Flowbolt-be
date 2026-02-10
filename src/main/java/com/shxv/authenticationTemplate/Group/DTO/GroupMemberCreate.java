package com.shxv.authenticationTemplate.Group.DTO;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberCreate {
    private UUID member;
}
