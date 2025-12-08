package com.hrmf.hrms_backend.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryDto {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Long totalDays;
    private Long presentDays;
    private Long absentDays;
    private Long halfDays;
    private Long totalWorkHours;
    private Long totalBreakHours;
    private Long averageWorkHoursPerDay;
    private List<DailySummaryDto> dailySummaries;

}
