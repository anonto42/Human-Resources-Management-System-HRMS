package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
}
