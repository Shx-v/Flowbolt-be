package com.shxv.flowbolt.Comment.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CommentCreate {

    private UUID ticketId;
    private List<CommentBlock> content;

}
