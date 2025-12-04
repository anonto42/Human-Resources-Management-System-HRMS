package com.hrmf.hrmf_project.constants;

import org.springframework.stereotype.Component;

@Component
public class SecurityConstants {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/api/v1/auth/login",
            "/api/v1/user/**",
    };

    public static boolean isPublicEndpoint(String uri) {

        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (uri.equals(endpoint)) {
                return true;
            }
        }
        return false;
    }
}