package com.hrmf.auth_service.dto;

import lombok.Builder;

@Builder

public class UserInfoDto {
    private String userId;
    private String email;
    private String fullName;
    private String role;
}