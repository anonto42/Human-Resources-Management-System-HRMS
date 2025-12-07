package com.hrmf.hrms_backend.dto.employee;

import com.hrmf.hrms_backend.dto.user.PersonalDetailsDto;
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
public class EmployeeDetailDto {
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private String employeeRole;
    private String contactNumber;
    private String shift;
    private String officeTime;
    private UserStatus status;
    private PersonalDetailsDto personalDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}