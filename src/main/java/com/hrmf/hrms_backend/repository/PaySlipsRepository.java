package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.PaySlips;
import com.hrmf.hrms_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface PaySlipsRepository extends JpaRepository<PaySlips, UUID> {

    Page<PaySlips> findByEmployee(User employee, Pageable pageable);

    Page<PaySlips> findByEmployeeAndTransactionDateBetween(
            User employee,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Page<PaySlips> findByTransactionDateBetween(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Page<PaySlips> findByJobCategoryIgnoreCase(String jobCategory, Pageable pageable);

    Page<PaySlips> findByTransactionDateBetweenAndJobCategoryIgnoreCase(
            LocalDate startDate,
            LocalDate endDate,
            String jobCategory,
            Pageable pageable
    );

    Page<PaySlips> findByCreatedBy(User createdBy, Pageable pageable);

    Page<PaySlips> findByEmployeeNameContainingIgnoreCaseOrEmployee_IdContainingIgnoreCase(
            String employeeName,
            User employee,
            Pageable pageable
    );
}