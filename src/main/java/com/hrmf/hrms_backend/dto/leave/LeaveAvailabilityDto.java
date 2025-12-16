package com.hrmf.hrms_backend.dto.leave;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveAvailabilityDto {
    private boolean available;
    private String message;
    private LocalDate requestedStartDate;
    private LocalDate requestedEndDate;
    private int totalBusinessDays;
}