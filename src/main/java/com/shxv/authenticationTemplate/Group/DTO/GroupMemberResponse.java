package com.shxv.authenticationTemplate.Group.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class GroupMemberResponse {

    private UUID id;
    private UUID groupId;
    private String groupName;
    private UserResponse member;
    private UserResponse createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}
