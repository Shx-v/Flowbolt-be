package com.shxv.authenticationTemplate.Util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class ResponseEnvelope<T> {
    private boolean success;
    private int status;
    private String message;
    private T data;
}
