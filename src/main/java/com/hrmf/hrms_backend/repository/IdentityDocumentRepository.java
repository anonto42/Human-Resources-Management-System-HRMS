package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.IdentityDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, UUID> {
    List<IdentityDocument> findByUserId(UUID userId);
}