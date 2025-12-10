package com.hrmf.hrms_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "national_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NationalDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "national_id_number")
    private String nationalIdNumber;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "country_of_residency")
    private String countryOfResidence;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "document_ref_number")
    private String documentRefNumber;

    @Column(name = "other_document_issue_date")
    private LocalDate otherDocumentIssueDate;

    @Column(name = "other_document_expiry_date")
    private LocalDate otherDocumentExpiryDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
