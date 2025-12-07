package com.hrmf.hrms_backend.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerDashboardStatsDto {
    private Long totalEmployees;
    private Long activeEmployees;
    private Long blockedEmployees;
    private Long deletedEmployees;
}