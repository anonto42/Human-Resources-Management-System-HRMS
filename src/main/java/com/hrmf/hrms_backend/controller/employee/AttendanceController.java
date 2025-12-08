package com.hrmf.hrms_backend.controller.employee;

import com.hrmf.hrms_backend.dto.attendance.*;
import com.hrmf.hrms_backend.service.AttendanceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/employee/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceManagementService attendanceManagementService;

    // Start attendance
    @PostMapping("/start")
    public ResponseEntity<?> startAttendance(@Valid @RequestBody StartAttendanceRequest request) {
        AttendanceResponseDto response = attendanceManagementService.startAttendance(request);
        return ResponseEntity.ok(response);
    }

    // Start break
    @PostMapping("/break/start")
    public ResponseEntity<?> startBreak() {
        AttendanceResponseDto response = attendanceManagementService.startBreak();
        return ResponseEntity.ok(response);
    }

    // End break
    @PostMapping("/break/end")
    public ResponseEntity<?> endBreak() {
        AttendanceResponseDto response = attendanceManagementService.endBreak();
        return ResponseEntity.ok(response);
    }

    // Start pause
    @PostMapping("/pause/start")
    public ResponseEntity<?> startPause() {
        AttendanceResponseDto response = attendanceManagementService.startPause();
        return ResponseEntity.ok(response);
    }

    // End pause
    @PostMapping("/pause/end")
    public ResponseEntity<?> endPause() {
        AttendanceResponseDto response = attendanceManagementService.endPause();
        return ResponseEntity.ok(response);
    }

    // Check out
    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@Valid @RequestBody CheckOutRequest request) {
        AttendanceResponseDto response = attendanceManagementService.checkOut(request);
        return ResponseEntity.ok(response);
    }

    // Get today's attendance
    @GetMapping("/today")
    public ResponseEntity<?> getTodayAttendance() {
        AttendanceResponseDto response = attendanceManagementService.getTodayAttendance();
        return ResponseEntity.ok(response);
    }

    // Get attendance summary
    @GetMapping("/summary")
    public ResponseEntity<?> getAttendanceSummary(
            @RequestParam String period,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) int week) {

        AttendanceSummaryDto response = attendanceManagementService.getAttendanceSummary(period, year, month, week, page, size);
        return ResponseEntity.ok(response);
    }

    // Get attendance history
    @GetMapping("/history")
    public ResponseEntity<?> getAttendanceHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PaginatedAttendanceResponse response = attendanceManagementService.getAttendanceHistory(page, size, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
