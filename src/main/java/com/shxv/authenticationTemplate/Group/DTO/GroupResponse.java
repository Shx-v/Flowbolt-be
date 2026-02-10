package com.shxv.authenticationTemplate.Group.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shxv.authenticationTemplate.Auth.DTO.UserListResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class GroupResponse {

    private UUID id;
    private String name;
    private String description;
    private String status;
    private UUID leader;
    private UUID createdBy;
    private List<UserListResponse> members;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}
