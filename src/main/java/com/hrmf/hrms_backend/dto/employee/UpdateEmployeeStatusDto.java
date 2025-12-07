package com.hrmf.hrms_backend.dto.employee;

import com.hrmf.hrms_backend.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeStatusDto {
    @NotNull(message = "Status is required")
    private UserStatus status;
}