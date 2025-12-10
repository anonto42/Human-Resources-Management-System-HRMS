package com.hrmf.hrms_backend.entity;

import com.hrmf.hrms_backend.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "identity_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number")
    private String documentNumber;

    private String nationality;
    private String country;

    @Column(name = "share_code")
    private String shareCode;

    @Column(name = "immigration_status")
    private String immigrationStatus;

    @Column(name = "country_residency")
    private String countryResidency;

    @Column(name = "issued_by")
    private String issuedBy;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_current")
    private Boolean isCurrent = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "document_files", columnDefinition = "jsonb")
    private Map<String, Object> documentFiles;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}