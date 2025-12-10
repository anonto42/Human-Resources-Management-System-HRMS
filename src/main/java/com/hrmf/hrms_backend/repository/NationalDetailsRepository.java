package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.NationalDetails;
import com.hrmf.hrms_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NationalDetailsRepository extends JpaRepository<NationalDetails, UUID> {
    Optional<NationalDetails> findFirstByUser(User user);
    List<NationalDetails> findByUser(User user);
}
