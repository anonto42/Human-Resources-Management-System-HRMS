package com.hrmf.hrms_backend.dto.user;

import com.hrmf.hrms_backend.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityDocumentDto {
    private UUID id;
    private DocumentType documentType;
    private String documentNumber;
    private String nationality;
    private String country;
    private String issuedBy;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Boolean isCurrent;
    private Map<String, Object> documentFiles;
    private LocalDateTime createdAt;
}