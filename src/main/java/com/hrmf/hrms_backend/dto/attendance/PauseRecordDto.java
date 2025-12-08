package com.hrmf.hrms_backend.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PauseRecordDto {
    private String pauseId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long durationMinutes;
    private Boolean isActive;
}