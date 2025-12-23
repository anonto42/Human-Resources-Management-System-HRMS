package com.hrmf.hrms_backend.document;

import com.hrmf.hrms_backend.enums.AttendanceApproval;
import com.hrmf.hrms_backend.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = "attendances")
@Data
@Builder
@NoArgsConstructor
public class Attendance {
    @Id
    private String id;

    private String userId;
    private String employerId;
    private String employeeName;
    private String employeeEmail;

    private LocalDate attendanceDate;
    private AttendanceStatus status;

    // Work timings
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Long totalWorkMinutes;

    // Break management
    @Builder.Default
    private List<BreakRecord> breaks = new ArrayList<>();
    private Long totalBreakMinutes;

    // Pause management
    @Builder.Default
    private List<PauseRecord> pauses = new ArrayList<>();
    private Long totalPauseMinutes;

    // Notes
    private String checkOutNote;
    private String adminNote;

    // Approval
    private AttendanceApproval approvalStatus;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructor
    public Attendance(String id, String userId, String employerId, String employeeName,
                      String employeeEmail, LocalDate attendanceDate, AttendanceStatus status,
                      LocalTime checkInTime, LocalTime checkOutTime, Long totalWorkMinutes,
                      List<BreakRecord> breaks, Long totalBreakMinutes,
                      List<PauseRecord> pauses, Long totalPauseMinutes,
                      String checkOutNote, String adminNote, AttendanceApproval approvalStatus,
                      String approvedBy, LocalDateTime approvedAt, String rejectionReason,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.employerId = employerId;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.totalWorkMinutes = totalWorkMinutes;
        this.breaks = breaks != null ? new ArrayList<>(breaks) : new ArrayList<>();
        this.totalBreakMinutes = totalBreakMinutes;
        this.pauses = pauses != null ? new ArrayList<>(pauses) : new ArrayList<>();
        this.totalPauseMinutes = totalPauseMinutes;
        this.checkOutNote = checkOutNote;
        this.adminNote = adminNote;
        this.approvalStatus = approvalStatus;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Custom getter for breaks - returns unmodifiable copy
    public List<BreakRecord> getBreaks() {
        return Collections.unmodifiableList(breaks);
    }

    // Custom setter for breaks
    public void setBreaks(List<BreakRecord> breaks) {
        this.breaks = breaks != null ? new ArrayList<>(breaks) : new ArrayList<>();
    }

    // Custom getter for pauses - returns unmodifiable copy
    public List<PauseRecord> getPauses() {
        return Collections.unmodifiableList(pauses);
    }

    // Custom setter for pauses
    public void setPauses(List<PauseRecord> pauses) {
        this.pauses = pauses != null ? new ArrayList<>(pauses) : new ArrayList<>();
    }

    // Helper methods to add items safely
    public void addBreak(BreakRecord breakRecord) {
        if (breakRecord != null) {
            breaks.add(breakRecord);
        }
    }

    public void addPause(PauseRecord pauseRecord) {
        if (pauseRecord != null) {
            pauses.add(pauseRecord);
        }
    }

    // Helper methods
    public boolean isCheckedIn() {
        return checkInTime != null && checkOutTime == null;
    }

    public boolean isOnBreak() {
        return !breaks.isEmpty() && breaks.stream().anyMatch(BreakRecord::isActive);
    }

    public boolean isPaused() {
        return !pauses.isEmpty() && pauses.stream().anyMatch(PauseRecord::isActive);
    }
}