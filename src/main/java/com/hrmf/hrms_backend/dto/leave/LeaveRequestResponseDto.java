package com.hrmf.hrms_backend.dto.leave;

import com.hrmf.hrms_backend.enums.LeaveStatus;
import com.hrmf.hrms_backend.enums.LeaveType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LeaveRequestResponseDto {
    private UUID id;
    private String employeeName;
    private UUID employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private LeaveStatus status;
    private String jobCategory;
    private String employeeProfileImage;
    private String designation;
    private String emergencyContact;
    private String approvedByEmail;
    private String approvedByName;
    private String employeeEmail;
    private LocalDateTime updatedAt;
    private LocalDateTime approvalDate;
    private String rejectionReason;
    private LocalDateTime createdAt;
}