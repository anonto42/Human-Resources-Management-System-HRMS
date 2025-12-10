package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.DocumentVerification;
import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentVerificationRepository extends JpaRepository<DocumentVerification, UUID> {
    List<DocumentVerification> findByUserOrderByCreatedAtDesc(User user);
    List<DocumentVerification> findByVerificationStatus(VerificationStatus status);
    Optional<DocumentVerification> findByUserAndVerificationStatus(User user, VerificationStatus status);
}