package com.shxv.flowbolt.Comment.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextBlock extends CommentBlock {
    private String type = "text";
    private String text;
}

