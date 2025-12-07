package com.hrmf.hrms_backend.exception;

public class SubAdminNotFoundException extends RuntimeException {
    public SubAdminNotFoundException(String message) {
        super(message);
    }

    public SubAdminNotFoundException(Long id) {
        super("Sub-admin not found with id: " + id);
    }
}