package com.hrmf.hrms_backend.dto.superAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubAdminStatusUpdateDto {
    private String status; // "ACTIVE", "BLOCKED", etc.
}