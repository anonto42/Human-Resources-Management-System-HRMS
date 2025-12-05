package com.hrmf.hrms_backend.dto.employer;

import com.hrmf.hrms_backend.enums.UserRole;
import lombok.Builder;

@Builder
public class AddEmployeeResponseDto {
    private String id;
    private String name;
    private String gender;
    private String email;
    private UserRole role;
}
