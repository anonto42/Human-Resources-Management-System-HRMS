package com.hrmf.hrms_backend.dto.auth;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class SignInResponseDto {
    private String accessToken;
    private String refreshToken;
    private Map<String, String> user;
}
