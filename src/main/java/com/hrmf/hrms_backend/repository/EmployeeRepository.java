package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.Employee;
import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    // Basic queries
    List<Employee> findByEmployer(User employer);
    Page<Employee> findByEmployer(User employer, Pageable pageable);

    Optional<Employee> findByIdAndEmployer(UUID id, User employer);

    // Find by user email
    Optional<Employee> findByUserEmailAndEmployer(String email, User employer);

    // Find by user status
    List<Employee> findByEmployerAndUserStatus(User employer, UserStatus status);
    Page<Employee> findByEmployerAndUserStatus(User employer, UserStatus status, Pageable pageable);

    // Count queries
    long countByEmployer(User employer);
    long countByEmployerAndUserStatus(User employer, UserStatus status);

    // For search functionality
    @Query("SELECT e FROM Employee e WHERE e.employer = :employer AND " +
            "(LOWER(e.user.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.user.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Employee> searchByEmployerAndNameOrEmail(
            @Param("employer") User employer,
            @Param("search") String search
    );

    @Query("SELECT e FROM Employee e WHERE e.employer = :employer AND " +
            "(LOWER(e.user.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.user.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchByEmployerAndNameOrEmail(
            @Param("employer") User employer,
            @Param("search") String search,
            Pageable pageable
    );
}