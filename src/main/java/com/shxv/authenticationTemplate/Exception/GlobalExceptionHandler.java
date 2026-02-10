package com.shxv.authenticationTemplate.Exception;

import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.r2dbc.BadSqlGrammarException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔥 Duplicate key (unique constraint)
    @ExceptionHandler(DuplicateKeyException.class)
    public Mono<ResponseEnvelope<Object>> handleDuplicateKey(DuplicateKeyException ex) {

        return Mono.just(
                ResponseEnvelope.builder()
                        .success(false)
                        .status(HttpStatus.CONFLICT.value())
                        .message(resolveDuplicateKeyMessage(ex))
                        .data(null)
                        .build()
        );
    }

    // 🔥 NOT NULL / FK violations / constraint errors
    @ExceptionHandler({DataIntegrityViolationException.class, R2dbcDataIntegrityViolationException.class})
    public Mono<ResponseEnvelope<Object>> handleIntegrityViolation(Exception ex) {
        return Mono.just(
                ResponseEnvelope.builder()
                        .success(false)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(resolveNotNullMessage(ex))
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEnvelope<Object>> handleForbidden(
            RuntimeException ex) {

        return Mono.just(
                ResponseEnvelope.builder()
                        .success(false)
                        .status(HttpStatus.FORBIDDEN.value())
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }


    // 🔥 Fallback for all other errors
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEnvelope<Object>> handleGeneralException(Exception ex) {
        return Mono.just(
                ResponseEnvelope.builder()
                        .success(false)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Something went wrong")
                        .data(null)
                        .build()
        );
    }


    // ---------------- HELPERS ---------------- //

    private String resolveDuplicateKeyMessage(Exception ex) {
        String msg = ex.getMessage();
        if (msg.contains("project_code")) return "Project code already exists";
        return "Duplicate key violation";
    }

    private String resolveNotNullMessage(Exception ex) {
        String msg = ex.getMessage();

        if (msg.contains("owner")) return "Owner is required";
        if (msg.contains("name")) return "Project name is required";
        if (msg.contains("project_code")) return "Project code is required";

        return "Missing required field";
    }
}
