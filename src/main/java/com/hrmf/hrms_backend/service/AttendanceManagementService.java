package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.document.Attendance;
import com.hrmf.hrms_backend.document.BreakRecord;
import com.hrmf.hrms_backend.document.PauseRecord;
import com.hrmf.hrms_backend.dto.attendance.*;
import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.enums.AttendanceApproval;
import com.hrmf.hrms_backend.enums.AttendanceStatus;
import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.exception.CustomException;
import com.hrmf.hrms_backend.repository.EmployeeRepository;
import com.hrmf.hrms_backend.repository.mongo.AttendanceRepository;
import com.hrmf.hrms_backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceManagementService {

    private final UserService userService;
    private final AttendanceRepository attendanceRepository;
    private final SecurityUtil securityUtil;
    private final EmployeeRepository employeeRepository;
    private final MongoTemplate mongoTemplate;

    // Check-in
    public AttendanceResponseDto startAttendance(StartAttendanceRequest request) {
        User currentUser = securityUtil.getCurrentUserOrThrow();

        // Check if user is an employee
        if (currentUser.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("Only employees can check in");
        }

        LocalDate today = LocalDate.now();

        // Check if already checked in today
        attendanceRepository.findByUserIdAndCheckOutTimeIsNullAndAttendanceDate(
                currentUser.getId().toString(), today
        ).ifPresent(att -> {
            throw new CustomException("You are already checked in for today", HttpStatus.ALREADY_REPORTED);
        });

        // Check if already has an attendance record for today
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndAttendanceDate(
                currentUser.getId().toString(), today
        );

        if (existingAttendance.isPresent()) {
            // If there's already an attendance record for today
            Attendance attendance = existingAttendance.get();

            // If already checked out, throw error
            if (attendance.getCheckOutTime() != null) {
                throw new CustomException("You have already completed attendance for today", HttpStatus.CONFLICT);
            }

            // If not checked out
            throw new CustomException("You have an existing attendance record for today", HttpStatus.CONFLICT);
        }

        // Create new attendance record
        Attendance attendance = Attendance.builder()
                .id(UUID.randomUUID().toString())
                .userId(currentUser.getId().toString())
                .employerId(getEmployerIdForEmployee(currentUser.getId()).toString())
                .employeeName(currentUser.getName())
                .employeeEmail(currentUser.getEmail())
                .attendanceDate(today)
                .status(AttendanceStatus.CHECKED_IN)
                .checkInTime(LocalTime.now())
                .totalWorkMinutes(0L)
                .totalBreakMinutes(0L)
                .totalPauseMinutes(0L)
                .approvalStatus(AttendanceApproval.PENDING)
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Employee {} checked in at {}", currentUser.getEmail(), savedAttendance.getCheckInTime());

        return convertToAttendanceResponseDto(savedAttendance);
    }

    // Start break
    public AttendanceResponseDto startBreak() {
        User currentUser = securityUtil.getCurrentUserOrThrow();
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByUserIdAndCheckOutTimeIsNullAndAttendanceDate(currentUser.getId().toString(), today)
                .orElseThrow(() -> new IllegalStateException("You are not checked in"));

        if (attendance.isOnBreak()) {
            throw new IllegalStateException("You are already on a break");
        }

        if (attendance.isPaused()) {
            throw new IllegalStateException("You cannot start a break while paused");
        }

        BreakRecord breakRecord = BreakRecord.builder()
                .breakId(UUID.randomUUID().toString())
                .startTime(LocalTime.now())
                .isActive(true)
                .build();

        attendance.getBreaks().add(breakRecord);
        attendance.setStatus(AttendanceStatus.ON_BREAK);

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Employee {} started break at {}", currentUser.getEmail(), breakRecord.getStartTime());

        return convertToAttendanceResponseDto(savedAttendance);
    }

    // End break
    public AttendanceResponseDto endBreak() {
        User currentUser = securityUtil.getCurrentUserOrThrow();
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByUserIdAndCheckOutTimeIsNullAndAttendanceDate(currentUser.getId().toString(), today)
                .orElseThrow(() -> new IllegalStateException("You are not checked in"));

        BreakRecord activeBreak = attendance.getBreaks().stream()
                .filter(BreakRecord::isActive)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active break found"));

        activeBreak.setEndTime(LocalTime.now());
        activeBreak.setDurationMinutes(Duration.between(
                activeBreak.getStartTime(), activeBreak.getEndTime()
        ).toMinutes());
        activeBreak.setIsActive(false);

        // Update total break minutes
        long totalBreakMinutes = attendance.getBreaks().stream()
                .mapToLong(br -> br.getDurationMinutes() != null ? br.getDurationMinutes() : 0)
                .sum();
        attendance.setTotalBreakMinutes(totalBreakMinutes);

        attendance.setStatus(AttendanceStatus.CHECKED_IN);

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Employee {} ended break at {}", currentUser.getEmail(), activeBreak.getEndTime());

        return convertToAttendanceResponseDto(savedAttendance);
    }

    // Start pause
    public AttendanceResponseDto startPause() {
        User currentUser = securityUtil.getCurrentUserOrThrow();
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByUserIdAndCheckOutTimeIsNullAndAttendanceDate(currentUser.getId().toString(), today)
                .orElseThrow(() -> new IllegalStateException("You are not checked in"));

        if (attendance.isOnBreak()) {
            throw new IllegalStateException("You cannot pause while on break");
        }

        if (attendance.isPaused()) {
            throw new IllegalStateException("You are already paused");
        }

        PauseRecord pauseRecord = PauseRecord.builder()
                .pauseId(UUID.randomUUID().toString())
                .startTime(LocalTime.now())
                .isActive(true)
                .build();

        attendance.getPauses().add(pauseRecord);
        attendance.setStatus(AttendanceStatus.PAUSED);

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Employee {} paused at {}", currentUser.getEmail(), pauseRecord.getStartTime());

        return convertToAttendanceResponseDto(savedAttendance);
    }

    // End pause
    public AttendanceResponseDto endPause() {
        User currentUser = securityUtil.getCurrentUserOrThrow();
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByUserIdAndCheckOutTimeIsNullAndAttendanceDate(currentUser.getId().toString(), today)
                .orElseThrow(() -> new IllegalStateException("You are not checked in"));

        PauseRecord activePause = attendance.getPauses().stream()
                .filter(PauseRecord::isActive)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active pause found"));

        activePause.setEndTime(LocalTime.now());
        activePause.setDurationMinutes(Duration.between(
                activePause.getStartTime(), activePause.getEndTime()
        ).toMinutes());
        activePause.setIsActive(false);

        // Update total pause minutes
        long totalPauseMinutes = attendance.getPauses().stream()
                .mapToLong(pr -> pr.getDurationMinutes() != null ? pr.getDurationMinutes() : 0)
                .sum();
        attendance.setTotalPauseMinutes(totalPauseMinutes);

        attendance.setStatus(AttendanceStatus.CHECKED_IN);

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Employee {} ended pause at {}", currentUser.getEmail(), activePause.getEndTime());

        return convertToAttendanceResponseDto(savedAttendance);
    }

    // Check out
    public AttendanceResponseDto checkOut(CheckOutRequest request) {
        User currentUser = securityUtil.getCurrentUserOrThrow();
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByUserIdAndCheckOutTimeIsNullAndAttendanceDate(currentUser.getId().toString(), today)
                .orElseThrow(() -> new CustomException("You are not checked in", HttpStatus.NOT_FOUND));

        if (attendance.isOnBreak()) {
            throw new IllegalStateException("Cannot check out while on break. End break first.");
        }

        if (attendance.isPaused()) {
            throw new IllegalStateException("Cannot check out while paused. End pause first.");
        }

        attendance.setCheckOutTime(LocalTime.now());

        // Calculate total work minutes (excluding breaks and pauses)
        long workMinutes = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
        long breakMinutes = attendance.getTotalBreakMinutes() != null ? attendance.getTotalBreakMinutes() : 0;
        long pauseMinutes = attendance.getTotalPauseMinutes() != null ? attendance.getTotalPauseMinutes() : 0;

        attendance.setTotalWorkMinutes(workMinutes - breakMinutes - pauseMinutes);
        attendance.setStatus(AttendanceStatus.CHECKED_OUT);
        attendance.setCheckOutNote(request.getNote());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Employee {} checked out at {}", currentUser.getEmail(), savedAttendance.getCheckOutTime());

        return convertToAttendanceResponseDto(savedAttendance);
    }

    // Get today's attendance
    public AttendanceResponseDto getTodayAttendance() {
        User currentUser = securityUtil.getCurrentUserOrThrow();
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByUserIdAndAttendanceDate(currentUser.getId().toString(), today)
                .orElseThrow(() -> new CustomException("No attendance record for today", HttpStatus.NOT_FOUND));

        return convertToAttendanceResponseDto(attendance);
    }

    // Get attendance summary
    public AttendanceSummaryDto getAttendanceSummary(String period, int year, int month, Integer week, int page, int size) {
        User currentUser = securityUtil.getCurrentUserOrThrow();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "attendanceDate"));
        Page<Attendance> attendancePage;

        LocalDate startDate;
        LocalDate endDate;

        if ("monthly".equalsIgnoreCase(period)) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } else if ("weekly".equalsIgnoreCase(period)) {
            startDate = LocalDate.of(year, month, 1).with(java.time.temporal.TemporalAdjusters.dayOfWeekInMonth(week, java.time.DayOfWeek.MONDAY));
            endDate = startDate.plusDays(6);
        } else {
            throw new IllegalArgumentException("Invalid period. Use 'weekly' or 'monthly'");
        }

        Page<Attendance> attendances = attendanceRepository
                .findByUserIdAndAttendanceDateBetween(currentUser.getId().toString(), startDate, endDate, pageable);

        return buildAttendanceSummary(attendances, startDate, endDate);
    }

    // Get attendance history with pagination
    public PaginatedAttendanceResponse getAttendanceHistory(int page, int size, LocalDate startDate, LocalDate endDate) {
        User currentUser = securityUtil.getCurrentUserOrThrow();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "attendanceDate"));
        Page<Attendance> attendancePage;

        if (startDate != null && endDate != null) {
            attendancePage = attendanceRepository.findByUserIdAndAttendanceDateBetween(
                    currentUser.getId().toString(), startDate, endDate, pageable
            );
        } else {
            LocalDate defaultStartDate = LocalDate.now().minusDays(30);
            LocalDate defaultEndDate = LocalDate.now();
            attendancePage = attendanceRepository.findByUserIdAndAttendanceDateBetween(
                    currentUser.getId().toString(), defaultStartDate, defaultEndDate, pageable
            );
        }

        List<AttendanceResponseDto> content = attendancePage.getContent().stream()
                .map(this::convertToAttendanceResponseDto)
                .collect(Collectors.toList());

        return PaginatedAttendanceResponse.builder()
                .content(content)
                .page(attendancePage.getNumber())
                .size(attendancePage.getSize())
                .totalElements(attendancePage.getTotalElements())
                .totalPages(attendancePage.getTotalPages())
                .last(attendancePage.isLast())
                .build();
    }

    // Get all active attendances
    public List<AttendanceResponseDto> getActiveAttendances(int page, int size) {
        User currentEmployer = securityUtil.getCurrentUserOrThrow();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "attendanceDate"));
        Page<Attendance> attendancePage;

        if (currentEmployer.getRole() != UserRole.EMPLOYER) {
            throw new IllegalArgumentException("Only employers can view active attendances");
        }

        attendancePage = attendanceRepository
                .findByEmployerIdAndStatus(currentEmployer.getId().toString(), AttendanceStatus.CHECKED_OUT.name(), pageable);

        return attendancePage.stream()
                .map(this::convertToAttendanceResponseDto)
                .collect(Collectors.toList());
    }

    // Get attendance by employee and date
    public AttendanceResponseDto getEmployeeAttendance(UUID employeeId, LocalDate date) {
        User currentEmployer = securityUtil.getCurrentUserOrThrow();

        if (currentEmployer.getRole() != UserRole.EMPLOYER) {
            throw new CustomException("Only employers can view employee attendance", HttpStatus.NOT_ACCEPTABLE);
        }

        Attendance attendance = attendanceRepository
                .findByUserIdAndAttendanceDate(employeeId.toString(), date)
                .orElseThrow(() -> new CustomException("Attendance not found", HttpStatus.NOT_FOUND));

        // Verify this employee belongs to the employer
        if (!attendance.getEmployerId().equals(currentEmployer.getId().toString())) {
            throw new CustomException("You don't have permission to view this attendance", HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }

        return convertToAttendanceResponseDto(attendance);
    }

    // Get pending attendances
    public PaginatedAttendanceResponse getPendingAttendances(int page, int size) {
        User currentEmployer = securityUtil.getCurrentUserOrThrow();

        if (currentEmployer.getRole() != UserRole.EMPLOYER) {
            throw new IllegalArgumentException("Only employers can view pending attendances");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "attendanceDate"));
        Page<Attendance> pendingPage = attendanceRepository.findByEmployerIdAndApprovalStatus(
                currentEmployer.getId().toString(), AttendanceApproval.PENDING.name(), pageable
        );

        List<AttendanceResponseDto> content = pendingPage.getContent().stream()
                .map(this::convertToAttendanceResponseDto)
                .collect(Collectors.toList());

        return PaginatedAttendanceResponse.builder()
                .content(content)
                .page(pendingPage.getNumber())
                .size(pendingPage.getSize())
                .totalElements(pendingPage.getTotalElements())
                .totalPages(pendingPage.getTotalPages())
                .last(pendingPage.isLast())
                .build();
    }

    // Approve/Reject attendance
    public AttendanceResponseDto updateAttendanceStatus(String attendanceId, UpdateAttendanceRequest request) {
        User currentEmployer = securityUtil.getCurrentUserOrThrow();

        if (currentEmployer.getRole() != UserRole.EMPLOYER) {
            throw new CustomException("Only employers can approve/reject attendance", HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new CustomException("Attendance not found", HttpStatus.NOT_FOUND));

        // Verify this attendance belongs to the employer
        if (!attendance.getEmployerId().equals(currentEmployer.getId().toString())) {
            throw new CustomException("You don't have permission to update this attendance", HttpStatus.NOT_ACCEPTABLE);
        }

        attendance.setAdminNote(request.getAdminNote());
        attendance.setApprovalStatus(AttendanceApproval.valueOf(request.getApprovalStatus()));

        if (AttendanceApproval.REJECTED.name().equals(request.getApprovalStatus())) {
            attendance.setRejectionReason(request.getRejectionReason());
        }

        attendance.setApprovedBy(currentEmployer.getId().toString());
        attendance.setApprovedAt(LocalDateTime.now());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Attendance {} updated to {} by {}",
                attendanceId, request.getApprovalStatus(), currentEmployer.getEmail());

        return convertToAttendanceResponseDto(savedAttendance);
    }

    // Get all attendances
    public PaginatedAttendanceResponse getAllAttendancesForEmployer(
            int page, int size,
            LocalDate startDate, LocalDate endDate,
            String status, String approvalStatus) {

        User currentEmployer = securityUtil.getCurrentUserOrThrow();

        if (currentEmployer.getRole() != UserRole.EMPLOYER) {
            throw new IllegalArgumentException("Only employers can view all attendances");
        }

        // Set default date range if not provided
        LocalDate defaultStartDate = LocalDate.now().minusMonths(1);
        LocalDate defaultEndDate = LocalDate.now();

        // Create main criteria for employer
        Criteria criteria = Criteria.where("employerId").is(currentEmployer.getId().toString());

        // Create date range criteria
        Criteria dateCriteria = new Criteria();
        if (startDate != null || endDate != null) {
            LocalDate finalStartDate = startDate != null ? startDate : defaultStartDate;
            LocalDate finalEndDate = endDate != null ? endDate : defaultEndDate;

            dateCriteria = Criteria.where("attendanceDate")
                    .gte(finalStartDate)
                    .lte(finalEndDate);
        } else {
            dateCriteria = Criteria.where("attendanceDate")
                    .gte(defaultStartDate)
                    .lte(defaultEndDate);
        }

        // Combine criteria
        criteria = criteria.andOperator(dateCriteria);

        // Add status filter if provided
        if (status != null && !status.isEmpty()) {
            criteria = criteria.and("status").is(status);
        }

        // Add approval status filter if provided
        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            criteria = criteria.and("approvalStatus").is(approvalStatus);
        }

        // Create pageable with sorting
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "attendanceDate", "checkInTime")
        );

        // Create query
        Query query = new Query(criteria).with(pageable);

        // Get total count
        long total = mongoTemplate.count(query, Attendance.class);

        // Get paginated results
        List<Attendance> attendances = mongoTemplate.find(query, Attendance.class);

        // Calculate pagination info
        int totalPages = (int) Math.ceil((double) total / size);
        boolean isLast = (page + 1) * size >= total;

        // Convert to DTOs
        List<AttendanceResponseDto> content = attendances.stream()
                .map(this::convertToAttendanceResponseDto)
                .collect(Collectors.toList());

        return PaginatedAttendanceResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .last(isLast)
                .build();
    }

    // helper

    private UUID getEmployerIdForEmployee(UUID employeeUserId) {
        // Method 1: Using the custom query method
        return employeeRepository.findEmployerIdByEmployeeUserId(employeeUserId)
                .orElseThrow(() -> {
                    log.error("No employer found for employee user ID: {}", employeeUserId);
                    throw new IllegalStateException("Employee not associated with any employer");
                });
    }

    private AttendanceSummaryDto buildAttendanceSummary(Page<Attendance> attendances, LocalDate startDate, LocalDate endDate) {
        long totalDays = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays() + 1;
        long presentDays = attendances.stream()
                .filter(att -> att.getStatus() != AttendanceStatus.ABSENT)
                .count();
        long absentDays = totalDays - presentDays;
        long halfDays = attendances.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.HALF_DAY)
                .count();

        long totalWorkMinutes = attendances.stream()
                .mapToLong(att -> att.getTotalWorkMinutes() != null ? att.getTotalWorkMinutes() : 0)
                .sum();
        long totalBreakMinutes = attendances.stream()
                .mapToLong(att -> att.getTotalBreakMinutes() != null ? att.getTotalBreakMinutes() : 0)
                .sum();

        List<DailySummaryDto> dailySummaries = attendances.stream()
                .map(att -> DailySummaryDto.builder()
                        .date(att.getAttendanceDate())
                        .status(att.getStatus().name())
                        .checkInTime(att.getCheckInTime())
                        .checkOutTime(att.getCheckOutTime())
                        .workHours(att.getTotalWorkMinutes() != null ? att.getTotalWorkMinutes() / 60 : 0)
                        .breakMinutes(att.getTotalBreakMinutes())
                        .pauseMinutes(att.getTotalPauseMinutes())
                        .build())
                .collect(Collectors.toList());

        return AttendanceSummaryDto.builder()
                .periodStart(startDate)
                .periodEnd(endDate)
                .totalDays(totalDays)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .halfDays(halfDays)
                .totalWorkHours(totalWorkMinutes / 60)
                .totalBreakHours(totalBreakMinutes / 60)
                .averageWorkHoursPerDay(presentDays > 0 ? (totalWorkMinutes / 60) / presentDays : 0)
                .dailySummaries(dailySummaries)
                .build();
    }

    private AttendanceResponseDto convertToAttendanceResponseDto(Attendance attendance) {
        return AttendanceResponseDto.builder()
                .id(attendance.getId())
                .userId(attendance.getUserId().toString())
                .employeeName(attendance.getEmployeeName())
                .employeeEmail(attendance.getEmployeeEmail())
                .attendanceDate(attendance.getAttendanceDate())
                .status(attendance.getStatus().name())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .totalWorkHours(attendance.getTotalWorkMinutes() != null ? attendance.getTotalWorkMinutes() / 60 : 0)
                .totalWorkMinutes(attendance.getTotalWorkMinutes())
                .totalBreakMinutes(attendance.getTotalBreakMinutes())
                .totalPauseMinutes(attendance.getTotalPauseMinutes())
                .checkOutNote(attendance.getCheckOutNote())
                .adminNote(attendance.getAdminNote())
                .approvalStatus(attendance.getApprovalStatus().name())
                .breaks(attendance.getBreaks().stream()
                        .map(br -> BreakRecordDto.builder()
                                .breakId(br.getBreakId())
                                .startTime(br.getStartTime())
                                .endTime(br.getEndTime())
                                .durationMinutes(br.getDurationMinutes())
                                .isActive(br.getIsActive())
                                .build())
                        .collect(Collectors.toList()))
                .pauses(attendance.getPauses().stream()
                        .map(pr -> PauseRecordDto.builder()
                                .pauseId(pr.getPauseId())
                                .startTime(pr.getStartTime())
                                .endTime(pr.getEndTime())
                                .durationMinutes(pr.getDurationMinutes())
                                .isActive(pr.getIsActive())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
