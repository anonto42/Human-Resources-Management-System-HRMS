package com.hrmf.hrms_backend.dto.subAdmin;

import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListDto {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private String designation;
    private String contactNumber;
    private String companyName;
    private UserStatus status;
    private LocalDateTime createdAt;
}