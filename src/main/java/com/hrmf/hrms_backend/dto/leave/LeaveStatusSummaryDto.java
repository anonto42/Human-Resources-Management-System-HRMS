package com.hrmf.hrms_backend.dto.leave;

import com.hrmf.hrms_backend.enums.LeaveStatus;
import com.hrmf.hrms_backend.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveStatusSummaryDto {
    private UUID employeeId;
    private String employeeName;
    private int year;
    private int month;
    private Map<LeaveStatus, Long> statusCounts;
    private Map<LeaveType, Long> typeCounts;
    private Long totalRequests;
    private Long pendingRequests;
    private Long approvedRequests;
    private Long rejectedRequests;
    private Long cancelledRequests;
}