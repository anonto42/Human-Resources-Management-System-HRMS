package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.PersonalDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalDetailsRepository extends JpaRepository<PersonalDetails, Integer> {
}
