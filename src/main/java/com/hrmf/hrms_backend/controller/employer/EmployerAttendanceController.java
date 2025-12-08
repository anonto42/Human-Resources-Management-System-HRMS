package com.hrmf.hrms_backend.controller.employer;

import com.hrmf.hrms_backend.dto.attendance.AttendanceResponseDto;
import com.hrmf.hrms_backend.dto.attendance.PaginatedAttendanceResponse;
import com.hrmf.hrms_backend.dto.attendance.UpdateAttendanceRequest;
import com.hrmf.hrms_backend.service.AttendanceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employer/attendance")
@RequiredArgsConstructor
public class EmployerAttendanceController {

    private final AttendanceManagementService attendanceManagementService;

    // Get all active attendances
    @GetMapping("/active")
    public ResponseEntity<?> getActiveAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<AttendanceResponseDto> response = attendanceManagementService.getActiveAttendances(page, size);
        return ResponseEntity.ok(response);
    }

    // Get employee attendance by date
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeAttendance(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AttendanceResponseDto response = attendanceManagementService.getEmployeeAttendance(employeeId, date);
        return ResponseEntity.ok(response);
    }

    // Get pending attendances for approval
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaginatedAttendanceResponse response = attendanceManagementService.getPendingAttendances(page, size);
        return ResponseEntity.ok(response);
    }

    // Approve/Reject attendance
    @PutMapping("/{attendanceId}/status")
    public ResponseEntity<?> updateAttendanceStatus(
            @PathVariable String attendanceId,
            @Valid @RequestBody UpdateAttendanceRequest request) {

        AttendanceResponseDto response = attendanceManagementService.updateAttendanceStatus(attendanceId, request);
        return ResponseEntity.ok(response);
    }

    // Get all attendances with filters
    @GetMapping("/all")
    public ResponseEntity<?> getAllAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String approvalStatus) {

        PaginatedAttendanceResponse response = attendanceManagementService.getAllAttendancesForEmployer(
                page, size, startDate, endDate, status, approvalStatus);

        return ResponseEntity.ok(response);
    }
}
