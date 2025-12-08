package com.hrmf.hrms_backend.repository.mongo;

import com.hrmf.hrms_backend.document.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {

    // Find today's attendance for a user
    Optional<Attendance> findByUserIdAndAttendanceDate(String userId, LocalDate date);

    // Find active attendance (checked in but not checked out)
    Optional<Attendance> findByUserIdAndCheckOutTimeIsNullAndAttendanceDate(String userId, LocalDate date);

    // Find all attendance for a user in date range
    Page<Attendance> findByUserIdAndAttendanceDateBetween(String userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Find attendance for an employer's employees
    List<Attendance> findByEmployerIdAndAttendanceDateBetween(String employerId, LocalDate startDate, LocalDate endDate);

    // Find by approval status
    Page<Attendance> findByEmployerIdAndApprovalStatus(String employerId, String approvalStatus, Pageable pageable);

    // Find by status
    Page<Attendance> findByEmployerIdAndStatus(String employerId, String status, Pageable pageable);
}