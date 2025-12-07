package com.hrmf.hrms_backend.dto.superAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubAdminStatsDto {
    private Long total;
    private Long active;
    private Long blocked;
    private Long deleted;
}