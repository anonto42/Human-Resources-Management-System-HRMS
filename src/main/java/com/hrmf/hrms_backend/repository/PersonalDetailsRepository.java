package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.PersonalDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonalDetailsRepository extends JpaRepository<PersonalDetails, UUID> {
    PersonalDetails findByUserId(UUID userId);
}
