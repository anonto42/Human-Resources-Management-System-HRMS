package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployerRepository extends JpaRepository<Employer, UUID> {
}
