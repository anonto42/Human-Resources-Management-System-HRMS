package com.hrmf.hrms_backend.dto.leave;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectLeaveRequestDto {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}