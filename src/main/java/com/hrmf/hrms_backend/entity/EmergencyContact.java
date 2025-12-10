package com.hrmf.hrms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "emergency_contacts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    public String relation;

    @Column(name = "give_details")
    public String giveDetails;
    public String email;

    @Column(name = "emergency_contact")
    public String emergencyContact;
    public String address;

    @Column(name = "title_of_certified_license")
    public String titleOfCertifiedLicense;

    @Column(name = "license_number")
    public String licenseNumber;

    @Column(name = "issue_date")
    public LocalDate issueDate;

    @Column(name = "expiry_date")
    public LocalDate expiryDate;
}