package com.hrmf.hrms_backend.dto.user;

import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.enums.UserStatus;
import lombok.Data;

@Data
public class UpdateUserDto {
    private String email;
    private String password;
    private UserRole role;
    private UserStatus status;
    private String imageUrl;
}