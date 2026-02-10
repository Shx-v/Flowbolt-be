package com.shxv.authenticationTemplate.Comment.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdate {

    private List<CommentBlock> content;

}
