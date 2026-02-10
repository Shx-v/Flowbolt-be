package com.shxv.authenticationTemplate.Group.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GroupCreate {

    private String name;
    private String description;
    private List<UUID> members;
}
