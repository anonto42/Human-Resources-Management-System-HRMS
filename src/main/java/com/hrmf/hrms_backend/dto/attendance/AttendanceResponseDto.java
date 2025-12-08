package com.hrmf.hrms_backend.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDto {
    private String id;
    private String userId;
    private String employeeName;
    private String employeeEmail;
    private LocalDate attendanceDate;
    private String status;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Long totalWorkHours;
    private Long totalWorkMinutes;
    private Long totalBreakMinutes;
    private Long totalPauseMinutes;
    private String checkOutNote;
    private String adminNote;
    private String approvalStatus;
    private List<BreakRecordDto> breaks;
    private List<PauseRecordDto> pauses;
}