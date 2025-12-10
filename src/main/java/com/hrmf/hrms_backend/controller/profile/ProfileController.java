package com.hrmf.hrms_backend.controller.profile;

import com.hrmf.hrms_backend.dto.profile.*;
import com.hrmf.hrms_backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // Get complete profile
    @GetMapping
    public ResponseEntity<ProfileResponse> getCompleteProfile() {
        ProfileResponse response = profileService.getCompleteProfile();
        return ResponseEntity.ok(response);
    }

    // Personal Details
    @GetMapping("/personal-details")
    public ResponseEntity<PersonalDetailsResponse> getPersonalDetails() {
        PersonalDetailsResponse response = profileService.getPersonalDetails();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/personal-details")
    public ResponseEntity<PersonalDetailsResponse> createOrUpdatePersonalDetails(
            @Valid @RequestBody UpdatePersonalDetailsDto request) {
        PersonalDetailsResponse response = profileService.createOrUpdatePersonalDetails(request);
        return ResponseEntity.ok(response);
    }

    // National ID Details
    @GetMapping("/national-details")
    public ResponseEntity<NationalIdDetailsResponse> getNationalIdDetails() {
        NationalIdDetailsResponse response = profileService.getNationalIdDetails();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/national-details")
    public ResponseEntity<NationalIdDetailsResponse> createOrUpdateNationalIdDetails(
            @Valid @RequestBody UpdateNationalIdDetails request) {
        NationalIdDetailsResponse response = profileService.createOrUpdateNationalIdDetails(request);
        return ResponseEntity.ok(response);
    }

    // Contact Information
    @GetMapping("/contact-information")
    public ResponseEntity<ContactInformationResponse> getContactInformation() {
        ContactInformationResponse response = profileService.getContactInformation();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/contact-information")
    public ResponseEntity<ContactInformationResponse> createOrUpdateContactInformation(
            @Valid @RequestBody UpdateContactInformationDto request) {
        ContactInformationResponse response = profileService.createOrUpdateContactInformation(request);
        return ResponseEntity.ok(response);
    }

    // Emergency Details
    @GetMapping("/emergency-details")
    public ResponseEntity<?> getEmergencyDetails() {
        var response = profileService.getEmergencyDetails();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/emergency-details")
    public ResponseEntity<?> createOrUpdateEmergencyDetails(
            @Valid @RequestBody UpdateEmergencyDetailsDto request) {
        var response = profileService.createOrUpdateEmergencyDetails(request);
        return ResponseEntity.ok(response);
    }

    // Education Details
    @GetMapping("/education-details")
    public ResponseEntity<?> getEducationDetails() {
        var response = profileService.getEducationDetails();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/education-details")
    public ResponseEntity<?> addEducationDetails(
            @Valid @RequestBody EducationDetailsRequestDto request) {
        var response = profileService.addEducationDetails(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/education-details/{id}")
    public ResponseEntity<?> updateEducationDetails(
            @PathVariable UUID id,
            @Valid @RequestBody EducationDetailsRequestDto request) {
        var response = profileService.updateEducationDetails(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/education-details/{id}")
    public ResponseEntity<?> removeEducationDetails(@PathVariable UUID id) {
        profileService.removeEducationDetails(id);
        return ResponseEntity.ok("Education details removed successfully");
    }

    // Job Details
    @GetMapping("/job-details")
    public ResponseEntity<?> getJobDetails() {
        var response = profileService.getJobDetails();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/job-details")
    public ResponseEntity<?> addJobDetails(
            @Valid @RequestBody JobDetailsRequestDto request) {
        var response = profileService.addJobDetails(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/job-details/{id}")
    public ResponseEntity<?> updateJobDetails(
            @PathVariable UUID id,
            @Valid @RequestBody JobDetailsRequestDto request) {
        var response = profileService.updateJobDetails(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/job-details/{id}")
    public ResponseEntity<?> removeJobDetails(@PathVariable UUID id) {
        profileService.removeJobDetails(id);
        return ResponseEntity.ok("Job details removed successfully");
    }

    // Service Details
    @GetMapping("/service-details")
    public ResponseEntity<?> getServiceDetails() {
        var response = profileService.getServiceDetails();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/service-details")
    public ResponseEntity<?> createOrUpdateServiceDetails(
            @Valid @RequestBody UpdateServiceDetailsDto request) {
        var response = profileService.createOrUpdateServiceDetails(request);
        return ResponseEntity.ok(response);
    }

    // Visa & Passport Details
    @GetMapping("/visa-passport-details")
    public ResponseEntity<?> getVisaPassportDetails() {
        var response = profileService.getVisaPassportDetails();
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/visa-passport-details/passport",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createOrUpdatePassport(
            @Valid @ModelAttribute UpdatePassportDetailsDto request) {
        var response = profileService.createOrUpdatePassport(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/visa-passport-details/visa",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createOrUpdateVisa(
            @Valid @ModelAttribute UpdateVisaDetailsDto request) {
        var response = profileService.createOrUpdateVisa(request);
        return ResponseEntity.ok(response);
    }

    // Training Details
    @GetMapping("/training-details")
    public ResponseEntity<?> getTrainingDetails() {
        var response = profileService.getTrainingDetails();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/training-details")
    public ResponseEntity<?> addTrainingDetails(
            @Valid @RequestBody TrainingDetailsRequestDto request) {
        var response = profileService.addTrainingDetails(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/training-details/{id}")
    public ResponseEntity<?> updateTrainingDetails(
            @PathVariable UUID id,
            @Valid @RequestBody TrainingDetailsRequestDto request) {
        var response = profileService.updateTrainingDetails(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/training-details/{id}")
    public ResponseEntity<?> removeTrainingDetails(@PathVariable UUID id) {
        profileService.removeTrainingDetails(id);
        return ResponseEntity.ok("Training details removed successfully");
    }

    // Company Information
    @GetMapping("/company-details")
    public ResponseEntity<?> getCompanyInformation() {
        var response = profileService.getCompanyInformation();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/company-details")
    public ResponseEntity<?> createOrUpdateCompanyInformation(
            @Valid @RequestBody UpdateCompanyInformationDto request) {
        var response = profileService.createOrUpdateCompanyInformation(request);
        return ResponseEntity.ok(response);
    }

    // Document Verification
    @PostMapping("/document-verification/submit")
    public ResponseEntity<?> submitDocumentForVerification(
            @Valid @ModelAttribute SubMitDocumentForVerificationDto request) {
        var response = profileService.submitDocumentForVerification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/document-verification/status")
    public ResponseEntity<?> getDocumentVerificationStatus() {
        var response = profileService.getDocumentVerificationStatus();
        return ResponseEntity.ok(response);
    }
}