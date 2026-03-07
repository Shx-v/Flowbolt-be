package com.shxv.flowbolt.Comment.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentionBlock extends CommentBlock {
    private String type = "mention";
    private UUID userId;
    private String label;
}

