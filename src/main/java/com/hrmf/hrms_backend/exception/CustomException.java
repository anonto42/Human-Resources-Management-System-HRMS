package com.hrmf.hrms_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final String customMessage;

    public CustomException(String message) {
        super(message);
        this.customMessage = message;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR; // Default
    }

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.customMessage = message;
        this.status = status;
    }

    public CustomException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.customMessage = message;
        this.status = status;
    }
}