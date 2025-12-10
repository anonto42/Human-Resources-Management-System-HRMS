package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.dto.profile.*;
import com.hrmf.hrms_backend.dto.profile.UpdateCompanyInformationDto;
import com.hrmf.hrms_backend.entity.*;
import com.hrmf.hrms_backend.enums.*;
import com.hrmf.hrms_backend.exception.CustomException;
import com.hrmf.hrms_backend.repository.*;
import com.hrmf.hrms_backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final IdentityDocumentRepository identityDocumentRepository;
    private final NationalDetailsRepository nationalDetailsRepository;
    private final AddressRepository addressRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final EducationDetailsRepository educationDetailsRepository;
    private final JobDetailsRepository jobDetailsRepository;
    private final ServiceDetailsRepository serviceDetailsRepository;
    private final TrainingDetailsRepository trainingDetailsRepository;
    private final CompanyInformationRepository companyInformationRepository;
    private final DocumentVerificationRepository documentVerificationRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getCompleteProfile() {
        User user = securityUtil.getCurrentUserOrThrow();

        return ProfileResponse.builder()
                .personalDetails(getPersonalDetailsResponse(user))
                .nationalIdDetails(getNationalIdDetailsResponse(user))
                .contactInformation(getContactInformationResponse(user))
                .emergencyDetails(getEmergencyDetailsResponse(user))
                .educationDetails(getEducationDetailsResponse(user))
                .jobDetails(getJobDetailsResponse(user))
                .serviceDetails(getServiceDetailsResponse(user))
                .visaPassportDetails(getVisaPassportDetailsResponse(user))
                .trainingDetails(getTrainingDetailsResponse(user))
                .companyInformation(getCompanyInformationResponse(user))
                .documentVerification(getDocumentVerificationResponse(user))
                .build();
    }

    @Transactional
    public PersonalDetailsResponse createOrUpdatePersonalDetails(UpdatePersonalDetailsDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        PersonalDetails personalDetails = personalDetailsRepository.findByUser(user)
                .orElseGet(() -> {
                    PersonalDetails newDetails = new PersonalDetails();
                    newDetails.setUser(user);
                    return newDetails;
                });

        personalDetails.setAbout(request.getAbout());
        personalDetails.setEmployerCode(request.getEmployerCode());
        personalDetails.setNiNo(request.getNiNo());

        // Convert gender string to enum
        if (request.getGender() != null) {
            try {
                personalDetails.setGender(Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new CustomException("Invalid gender value", HttpStatus.BAD_REQUEST);
            }
        }

        personalDetails.setDateOfBirth(request.getDateOfBirth());

        // Convert marital status string to enum
        if (request.getMaritalStatus() != null) {
            try {
                personalDetails.setMaritalStatus(MaritalStatus.valueOf(String.valueOf(request.getMaritalStatus())));
            } catch (IllegalArgumentException e) {
                throw new CustomException("Invalid marital status value", HttpStatus.BAD_REQUEST);
            }
        }

        PersonalDetails saved = personalDetailsRepository.save(personalDetails);

        return convertToPersonalDetailsResponse(saved);
    }

    @Transactional(readOnly = true)
    public PersonalDetailsResponse getPersonalDetails() {
        User user = securityUtil.getCurrentUserOrThrow();
        PersonalDetails personalDetails = personalDetailsRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("Personal details not found", HttpStatus.NOT_FOUND));

        return convertToPersonalDetailsResponse(personalDetails);
    }

    @Transactional
    public NationalIdDetailsResponse createOrUpdateNationalIdDetails(UpdateNationalIdDetails request) {
        User user = securityUtil.getCurrentUserOrThrow();

        // Find existing national ID document
        NationalDetails nationalId = nationalDetailsRepository
                .findFirstByUser(user)
                .orElse(NationalDetails.builder()
                        .user(user)
                        .nationalIdNumber(request.getNationalIdNumber())
                        .nationality(request.getNationality())
                        .countryOfResidence(request.getCountryOfResidence())
                        .issueDate(request.getIssueDate())
                        .expiryDate(request.getExpiryDate())
                        .documentName(request.getDocumentName())
                        .documentRefNumber(request.getDocumentRefNumber())
                        .otherDocumentIssueDate(request.getOtherDocumentIssueDate())
                        .otherDocumentExpiryDate(request.getExpiryDate())
                        .build());



        NationalDetails saved = nationalDetailsRepository.save(nationalId);

        return convertToNationalIdDetailsResponse(saved);
    }

    @Transactional(readOnly = true)
    public NationalIdDetailsResponse getNationalIdDetails() {
        User user = securityUtil.getCurrentUserOrThrow();
        NationalDetails nationalId = nationalDetailsRepository
                .findFirstByUser(user)
                .orElseThrow(() -> new CustomException("National ID details not found", HttpStatus.NOT_FOUND));

        return convertToNationalIdDetailsResponse(nationalId);
    }

    @Transactional
    public ContactInformationResponse createOrUpdateContactInformation(UpdateContactInformationDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        // Find primary address or create new one
        Address address = addressRepository.findByUserAndIsPrimary(user, true)
                .orElse(Address.builder()
                        .user(user)
                        .addressType(AddressType.PRIMARY)
                        .isPrimary(true)
                        .build());

        address.setPostCode(request.getPostCode());
        address.setAddress1(request.getAddress1());
        address.setAddress2(request.getAddress2());
        address.setCity(request.getCity());
        address.setCountry(request.getCountry());

        Address saved = addressRepository.save(address);

        // Update user's email if provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }

        return convertToContactInformationResponse(saved);
    }

    @Transactional(readOnly = true)
    public ContactInformationResponse getContactInformation() {
        User user = securityUtil.getCurrentUserOrThrow();
        Address address = addressRepository.findByUserAndIsPrimary(user, true)
                .orElseThrow(() -> new CustomException("Contact information not found", HttpStatus.NOT_FOUND));

        return convertToContactInformationResponse(address);
    }

    @Transactional
    public EmergencyDetailsResponse createOrUpdateEmergencyDetails(UpdateEmergencyDetailsDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        // Find first emergency contact or create new one
        List<EmergencyContact> existingContacts = emergencyContactRepository.findByUser(user);
        EmergencyContact emergencyContact;

        if (existingContacts.isEmpty()) {
            emergencyContact = EmergencyContact.builder()
                    .user(user)
                    .build();
        } else {
            emergencyContact = existingContacts.get(0);
        }

        emergencyContact.setFullName(request.getName());
        emergencyContact.setRelationship(request.getRelation());
        emergencyContact.setContactNumber(request.getEmergencyContact());
        emergencyContact.setEmail(request.getEmail());
        emergencyContact.setAddress(request.getAddress());
        emergencyContact.setAdditionalDetails(request.getGiveDetails());

        EmergencyContact saved = emergencyContactRepository.save(emergencyContact);

        return convertToEmergencyDetailsResponse(saved);
    }

    @Transactional
    public List<EmergencyDetailsResponse> addEmergencyContact(UpdateEmergencyDetailsDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        EmergencyContact emergencyContact = EmergencyContact.builder()
                .user(user)
                .fullName(request.getName())
                .relationship(request.getRelation())
                .contactNumber(request.getEmergencyContact())
                .email(request.getEmail())
                .address(request.getAddress())
                .additionalDetails(request.getGiveDetails())
                .build();

        EmergencyContact saved = emergencyContactRepository.save(emergencyContact);

        return getEmergencyDetailsResponse(user);
    }

    @Transactional
    public void removeEmergencyContact(UUID id) {
        User user = securityUtil.getCurrentUserOrThrow();
        EmergencyContact emergencyContact = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new CustomException("Emergency contact not found", HttpStatus.NOT_FOUND));

        if (!emergencyContact.getUser().getId().equals(user.getId())) {
            throw new CustomException("You can only remove your own emergency contacts", HttpStatus.FORBIDDEN);
        }

        emergencyContactRepository.delete(emergencyContact);
    }

    @Transactional(readOnly = true)
    public List<EmergencyDetailsResponse> getEmergencyDetails() {
        User user = securityUtil.getCurrentUserOrThrow();
        return getEmergencyDetailsResponse(user);
    }

    @Transactional
    public EducationDetailsResponse addEducationDetails(EducationDetailsRequestDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        EducationDetails educationDetails = EducationDetails.builder()
                .user(user)
                .degree(request.getDegree())
                .institute(request.getInstitute())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpDate())
                .passingYear(request.getPassingYear())
                .gradePoint(request.getGradePoint())
                .isCurrent(request.getExpDate() == null || request.getExpDate().isAfter(LocalDate.now()))
                .build();

        EducationDetails saved = educationDetailsRepository.save(educationDetails);

        return convertToEducationDetailsResponse(saved);
    }

    @Transactional
    public EducationDetailsResponse updateEducationDetails(UUID id, EducationDetailsRequestDto request) {
        User user = securityUtil.getCurrentUserOrThrow();
        EducationDetails educationDetails = educationDetailsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Education details not found", HttpStatus.NOT_FOUND));

        if (!educationDetails.getUser().getId().equals(user.getId())) {
            throw new CustomException("You can only update your own education details", HttpStatus.FORBIDDEN);
        }

        educationDetails.setDegree(request.getDegree());
        educationDetails.setInstitute(request.getInstitute());
        educationDetails.setIssueDate(request.getIssueDate());
        educationDetails.setExpiryDate(request.getExpDate());
        educationDetails.setPassingYear(request.getPassingYear());
        educationDetails.setGradePoint(request.getGradePoint());
        educationDetails.setIsCurrent(request.getExpDate() == null || request.getExpDate().isAfter(LocalDate.now()));

        EducationDetails saved = educationDetailsRepository.save(educationDetails);

        return convertToEducationDetailsResponse(saved);
    }

    @Transactional
    public void removeEducationDetails(UUID id) {
        User user = securityUtil.getCurrentUserOrThrow();
        EducationDetails educationDetails = educationDetailsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Education details not found", HttpStatus.NOT_FOUND));

        if (!educationDetails.getUser().getId().equals(user.getId())) {
            throw new CustomException("You can only remove your own education details", HttpStatus.FORBIDDEN);
        }

        educationDetailsRepository.delete(educationDetails);
    }

    @Transactional(readOnly = true)
    public List<EducationDetailsResponse> getEducationDetails() {
        User user = securityUtil.getCurrentUserOrThrow();
        return getEducationDetailsResponse(user);
    }

    @Transactional
    public JobDetailsResponse addJobDetails(JobDetailsRequestDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        JobDetails jobDetails = JobDetails.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .jobTitle(request.getJobTitle())
                .departmentName(request.getDepartmentName())
                .jobDesignation(request.getJobDesignation())
                .yearOfExperience(request.getYearOfExperience())
                .employeeStatus(request.getEmployeeStatus())
                .joiningDate(request.getJoiningDate())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpDate())
                .isCurrent(request.getExpDate() == null || request.getExpDate().isAfter(LocalDate.now()))
                .build();

        JobDetails saved = jobDetailsRepository.save(jobDetails);

        return convertToJobDetailsResponse(saved);
    }

    @Transactional
    public JobDetailsResponse updateJobDetails(UUID id, JobDetailsRequestDto request) {
        User user = securityUtil.getCurrentUserOrThrow();
        JobDetails jobDetails = jobDetailsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Job details not found", HttpStatus.NOT_FOUND));

        if (!jobDetails.getUser().getId().equals(user.getId())) {
            throw new CustomException("You can only update your own job details", HttpStatus.FORBIDDEN);
        }

        jobDetails.setCompanyName(request.getCompanyName());
        jobDetails.setJobTitle(request.getJobTitle());
        jobDetails.setDepartmentName(request.getDepartmentName());
        jobDetails.setJobDesignation(request.getJobDesignation());
        jobDetails.setYearOfExperience(request.getYearOfExperience());
        jobDetails.setEmployeeStatus(request.getEmployeeStatus());
        jobDetails.setJoiningDate(request.getJoiningDate());
        jobDetails.setIssueDate(request.getIssueDate());
        jobDetails.setExpiryDate(request.getExpDate());
        jobDetails.setIsCurrent(request.getExpDate() == null || request.getExpDate().isAfter(LocalDate.now()));

        JobDetails saved = jobDetailsRepository.save(jobDetails);

        return convertToJobDetailsResponse(saved);
    }

    @Transactional
    public void removeJobDetails(UUID id) {
        User user = securityUtil.getCurrentUserOrThrow();
        JobDetails jobDetails = jobDetailsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Job details not found", HttpStatus.NOT_FOUND));

        if (!jobDetails.getUser().getId().equals(user.getId())) {
            throw new CustomException("You can only remove your own job details", HttpStatus.FORBIDDEN);
        }

        jobDetailsRepository.delete(jobDetails);
    }

    @Transactional(readOnly = true)
    public List<JobDetailsResponse> getJobDetails() {
        User user = securityUtil.getCurrentUserOrThrow();
        return getJobDetailsResponse(user);
    }

    @Transactional
    public ServiceDetailsResponse createOrUpdateServiceDetails(UpdateServiceDetailsDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        ServiceDetails serviceDetails = serviceDetailsRepository.findByUser(user)
                .orElse(ServiceDetails.builder()
                        .user(user)
                        .build());

        serviceDetails.setDepartment(request.getDepartment());
        serviceDetails.setDesignation(request.getDesignation());
        serviceDetails.setDateOfJoining(request.getDateOfJoining());
        serviceDetails.setEmploymentType(request.getEmploymentType());
        serviceDetails.setDateOfConfirmation(request.getDateOfConfirmation());
        serviceDetails.setContractStartDate(request.getContactStartDate());
        serviceDetails.setContractEndDate(request.getContactEndDate());

        ServiceDetails saved = serviceDetailsRepository.save(serviceDetails);

        return convertToServiceDetailsResponse(saved);
    }

    @Transactional(readOnly = true)
    public ServiceDetailsResponse getServiceDetails() {
        User user = securityUtil.getCurrentUserOrThrow();
        ServiceDetails serviceDetails = serviceDetailsRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("Service details not found", HttpStatus.NOT_FOUND));

        return convertToServiceDetailsResponse(serviceDetails);
    }

    @Transactional
    public VisaPassportResponse getVisaPassportDetails() {
        User user = securityUtil.getCurrentUserOrThrow();
        return getVisaPassportDetailsResponse(user);
    }

    @Transactional
    public IdentityDocumentResponse createOrUpdatePassport(UpdatePassportDetailsDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        // Find existing passport or create new one
        IdentityDocument passport = identityDocumentRepository
                .findFirstByUserAndDocumentType(user, DocumentType.PASSPORT)
                .orElse(IdentityDocument.builder()
                        .user(user)
                        .documentType(DocumentType.PASSPORT)
                        .build());

        passport.setDocumentNumber(request.getPassportNumber());
        passport.setNationality(request.getNationality());
        passport.setIssuedBy(request.getIssueBy());
        passport.setIssueDate(request.getIssueDate());
        passport.setExpiryDate(request.getExpDate());
        passport.setIsCurrent(request.getIsCurrent());

        // Handle document files if needed
        if (request.getDocument() != null && request.getDocument().length > 0) {
            Map<String, Object> documentFiles = new HashMap<>();
            documentFiles.put("documents", Arrays.asList(request.getDocument()));
            passport.setDocumentFiles(documentFiles);
        }

        IdentityDocument saved = identityDocumentRepository.save(passport);

        return convertToIdentityDocumentResponse(saved);
    }

    @Transactional
    public IdentityDocumentResponse createOrUpdateVisa(UpdateVisaDetailsDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        IdentityDocument visa = identityDocumentRepository
                .findFirstByUserAndDocumentType(user, DocumentType.VISA)
                .orElse(IdentityDocument.builder()
                        .user(user)
                        .documentType(DocumentType.VISA)
                        .build());

        visa.setNationality(request.getNationality());
        visa.setCountry(request.getCountry());
        visa.setIssuedBy(request.getIssueBy());
        visa.setIssueDate(request.getIssueDate());
        visa.setExpiryDate(request.getExpDate());
        visa.setIsCurrent(request.getIsCurrentVisa());

        // Handle document files if needed
        if (request.getDocuments() != null && request.getDocuments().length > 0) {
            Map<String, Object> documentFiles = new HashMap<>();
            documentFiles.put("documents", Arrays.asList(request.getDocuments()));
            visa.setDocumentFiles(documentFiles);
        }

        IdentityDocument saved = identityDocumentRepository.save(visa);

        return convertToIdentityDocumentResponse(saved);
    }

    @Transactional
    public TrainingDetailsResponse addTrainingDetails(TrainingDetailsRequestDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        TrainingDetails trainingDetails = TrainingDetails.builder()
                .user(user)
                .trainingName(request.getTrainingName())
                .certificateName(request.getCertificateName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .expiryDate(request.getExpiryDate())
                .isCurrent(request.getExpiryDate() == null || request.getExpiryDate().isAfter(LocalDate.now()))
                .build();

        TrainingDetails saved = trainingDetailsRepository.save(trainingDetails);

        return convertToTrainingDetailsResponse(saved);
    }

    @Transactional
    public TrainingDetailsResponse updateTrainingDetails(UUID id, TrainingDetailsRequestDto request) {
        User user = securityUtil.getCurrentUserOrThrow();
        TrainingDetails trainingDetails = trainingDetailsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Training details not found", HttpStatus.NOT_FOUND));

        if (!trainingDetails.getUser().getId().equals(user.getId())) {
            throw new CustomException("You can only update your own training details", HttpStatus.FORBIDDEN);
        }

        trainingDetails.setTrainingName(request.getTrainingName());
        trainingDetails.setCertificateName(request.getCertificateName());
        trainingDetails.setStartDate(request.getStartDate());
        trainingDetails.setEndDate(request.getEndDate());
        trainingDetails.setExpiryDate(request.getExpiryDate());
        trainingDetails.setIsCurrent(request.getExpiryDate() == null || request.getExpiryDate().isAfter(LocalDate.now()));

        TrainingDetails saved = trainingDetailsRepository.save(trainingDetails);

        return convertToTrainingDetailsResponse(saved);
    }

    @Transactional
    public void removeTrainingDetails(UUID id) {
        User user = securityUtil.getCurrentUserOrThrow();
        TrainingDetails trainingDetails = trainingDetailsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Training details not found", HttpStatus.NOT_FOUND));

        if (!trainingDetails.getUser().getId().equals(user.getId())) {
            throw new CustomException("You can only remove your own training details", HttpStatus.FORBIDDEN);
        }

        trainingDetailsRepository.delete(trainingDetails);
    }

    @Transactional(readOnly = true)
    public List<TrainingDetailsResponse> getTrainingDetails() {
        User user = securityUtil.getCurrentUserOrThrow();
        return getTrainingDetailsResponse(user);
    }

    @Transactional
    public CompanyInformationResponse createOrUpdateCompanyInformation(UpdateCompanyInformationDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        CompanyInformation companyInformation = companyInformationRepository.findByUser(user)
                .orElse(CompanyInformation.builder()
                        .user(user)
                        .build());

        companyInformation.setCompanyName(request.getCompanyName());
        companyInformation.setDescription(request.getDescription());
        companyInformation.setAboutCompany(request.getAboutCompany());
        companyInformation.setOverview(request.getOverview());
        companyInformation.setCompanyType(request.getCompanyType());
        companyInformation.setFounded(request.getFounded());
        companyInformation.setRevenue(request.getRevenue());
        companyInformation.setWebsite(request.getWebsite());
        companyInformation.setCompanyEmail(request.getCompanyEmail());
        companyInformation.setContactNumber(request.getContactNumber());
        companyInformation.setAddress(request.getAddress());

        CompanyInformation saved = companyInformationRepository.save(companyInformation);

        return convertToCompanyInformationResponse(saved);
    }

    @Transactional(readOnly = true)
    public CompanyInformationResponse getCompanyInformation() {
        User user = securityUtil.getCurrentUserOrThrow();
        CompanyInformation companyInformation = companyInformationRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("Company information not found", HttpStatus.NOT_FOUND));

        return convertToCompanyInformationResponse(companyInformation);
    }

    @Transactional
    public DocumentVerificationResponse submitDocumentForVerification(SubMitDocumentForVerificationDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        DocumentVerification documentVerification = DocumentVerification.builder()
                .user(user)
                .employeeName(request.getEmployeeName())
                .employeeId(request.getEmployeeId())
                .shareCode(request.getShareCode())
                .documentType(request.getDocumentType())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .documentUrl(request.getDocumentUrl())
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        DocumentVerification saved = documentVerificationRepository.save(documentVerification);

        return convertToDocumentVerificationResponse(saved);
    }

    @Transactional(readOnly = true)
    public DocumentVerificationResponse getDocumentVerificationStatus() {
        User user = securityUtil.getCurrentUserOrThrow();
        List<DocumentVerification> verifications = documentVerificationRepository.findByUserOrderByCreatedAtDesc(user);

        if (verifications.isEmpty()) {
            throw new CustomException("No document verification found", HttpStatus.NOT_FOUND);
        }

        return convertToDocumentVerificationResponse(verifications.get(0));
    }

    private PersonalDetailsResponse getPersonalDetailsResponse(User user) {
        return personalDetailsRepository.findByUser(user)
                .map(this::convertToPersonalDetailsResponse)
                .orElse(null);
    }

    private NationalIdDetailsResponse getNationalIdDetailsResponse(User user) {
        List<NationalDetails> nationalIds = nationalDetailsRepository.findByUser(user);

        if (nationalIds.isEmpty()) {
            return null;
        }

        NationalDetails nationalId = nationalIds.get(0);
        return convertToNationalIdDetailsResponse(nationalId);
    }

    private ContactInformationResponse getContactInformationResponse(User user) {
        return addressRepository.findByUserAndIsPrimary(user, true)
                .map(this::convertToContactInformationResponse)
                .orElse(null);
    }

    private List<EmergencyDetailsResponse> getEmergencyDetailsResponse(User user) {
        return emergencyContactRepository.findByUser(user).stream()
                .map(this::convertToEmergencyDetailsResponse)
                .collect(Collectors.toList());
    }

    private List<EducationDetailsResponse> getEducationDetailsResponse(User user) {
        return educationDetailsRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::convertToEducationDetailsResponse)
                .collect(Collectors.toList());
    }

    private List<JobDetailsResponse> getJobDetailsResponse(User user) {
        return jobDetailsRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::convertToJobDetailsResponse)
                .collect(Collectors.toList());
    }

    private ServiceDetailsResponse getServiceDetailsResponse(User user) {
        return serviceDetailsRepository.findByUser(user)
                .map(this::convertToServiceDetailsResponse)
                .orElse(null);
    }

    private VisaPassportResponse getVisaPassportDetailsResponse(User user) {
        List<IdentityDocument> passports = identityDocumentRepository.findByUserAndDocumentType(user, DocumentType.PASSPORT);
        List<IdentityDocument> visas = identityDocumentRepository.findByUserAndDocumentType(user, DocumentType.VISA);

        return VisaPassportResponse.builder()
                .passports(passports.stream().map(this::convertToIdentityDocumentResponse).collect(Collectors.toList()))
                .visas(visas.stream().map(this::convertToIdentityDocumentResponse).collect(Collectors.toList()))
                .build();
    }

    private List<TrainingDetailsResponse> getTrainingDetailsResponse(User user) {
        return trainingDetailsRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::convertToTrainingDetailsResponse)
                .collect(Collectors.toList());
    }

    private CompanyInformationResponse getCompanyInformationResponse(User user) {
        return companyInformationRepository.findByUser(user)
                .map(this::convertToCompanyInformationResponse)
                .orElse(null);
    }

    private DocumentVerificationResponse getDocumentVerificationResponse(User user) {
        List<DocumentVerification> verifications = documentVerificationRepository.findByUserOrderByCreatedAtDesc(user);
        if (!verifications.isEmpty()) {
            return convertToDocumentVerificationResponse(verifications.get(0));
        }
        return null;
    }

    private PersonalDetailsResponse convertToPersonalDetailsResponse(PersonalDetails details) {
        return PersonalDetailsResponse.builder()
                .id(details.getId().toString())
                .about(details.getAbout())
                .niNo(details.getNiNo())
                .employerCode(details.getEmployerCode())
                .gender(details.getGender() != null ? details.getGender().name() : null)
                .dateOfBirth(details.getDateOfBirth() != null ? LocalDate.parse(details.getDateOfBirth().toString()) : null)
                .maritalStatus(details.getMaritalStatus() != null ? details.getMaritalStatus().name() : null)
                .build();
    }

    private NationalIdDetailsResponse convertToNationalIdDetailsResponse(NationalDetails document) {
        return NationalIdDetailsResponse.builder()
                .id(document.getId().toString())
                .nationalIdNumber(document.getNationalIdNumber())
                .nationality(document.getNationality())
                .countryOfResidence(document.getCountryOfResidence())
                .issueDate(document.getIssueDate())
                .expiryDate(document.getExpiryDate())
                .documentName(document.getDocumentName())
                .documentRefNumber(document.getDocumentRefNumber())
                .otherDocumentIssueDate(document.getOtherDocumentIssueDate())
                .otherDocumentExpiryDate(document.getExpiryDate())
                .build();
    }

    private ContactInformationResponse convertToContactInformationResponse(Address address) {
        return ContactInformationResponse.builder()
                .id(address.getId().toString())
                .postCode(address.getPostCode())
                .address1(address.getAddress1())
                .address2(address.getAddress2())
                .address3(address.getAddress3())
                .city(address.getCity())
                .country(address.getCountry())
                .addressType(address.getAddressType() != null ? address.getAddressType().name() : null)
                .isPrimary(address.getIsPrimary())
                .build();
    }

    private EmergencyDetailsResponse convertToEmergencyDetailsResponse(EmergencyContact contact) {
        return EmergencyDetailsResponse.builder()
                .id(contact.getId().toString())
                .fullName(contact.getFullName())
                .relationship(contact.getRelationship())
                .contactNumber(contact.getContactNumber())
                .email(contact.getEmail())
                .address(contact.getAddress())
                .additionalDetails(contact.getAdditionalDetails())
                .build();
    }

    private EducationDetailsResponse convertToEducationDetailsResponse(EducationDetails details) {
        return EducationDetailsResponse.builder()
                .id(details.getId().toString())
                .degree(details.getDegree())
                .institute(details.getInstitute())
                .issueDate(details.getIssueDate() != null ? details.getIssueDate().toString() : null)
                .expiryDate(details.getExpiryDate() != null ? details.getExpiryDate().toString() : null)
                .passingYear(details.getPassingYear())
                .gradePoint(details.getGradePoint())
                .isCurrent(details.getIsCurrent())
                .createdAt(details.getCreatedAt())
                .build();
    }

    private JobDetailsResponse convertToJobDetailsResponse(JobDetails details) {
        return JobDetailsResponse.builder()
                .id(details.getId().toString())
                .companyName(details.getCompanyName())
                .jobTitle(details.getJobTitle())
                .departmentName(details.getDepartmentName())
                .jobDesignation(details.getJobDesignation())
                .yearOfExperience(details.getYearOfExperience())
                .employeeStatus(details.getEmployeeStatus())
                .joiningDate(details.getJoiningDate() != null ? details.getJoiningDate().toString() : null)
                .issueDate(details.getIssueDate() != null ? details.getIssueDate().toString() : null)
                .expiryDate(details.getExpiryDate() != null ? details.getExpiryDate().toString() : null)
                .isCurrent(details.getIsCurrent())
                .createdAt(details.getCreatedAt())
                .build();
    }

    private ServiceDetailsResponse convertToServiceDetailsResponse(ServiceDetails details) {
        return ServiceDetailsResponse.builder()
                .id(details.getId().toString())
                .department(details.getDepartment())
                .designation(details.getDesignation())
                .dateOfJoining(details.getDateOfJoining() != null ? details.getDateOfJoining().toString() : null)
                .employmentType(details.getEmploymentType())
                .dateOfConfirmation(details.getDateOfConfirmation() != null ? details.getDateOfConfirmation().toString() : null)
                .contractStartDate(details.getContractStartDate() != null ? details.getContractStartDate().toString() : null)
                .contractEndDate(details.getContractEndDate() != null ? details.getContractEndDate().toString() : null)
                .createdAt(details.getCreatedAt())
                .updatedAt(details.getUpdatedAt())
                .build();
    }

    private IdentityDocumentResponse convertToIdentityDocumentResponse(IdentityDocument document) {
        return IdentityDocumentResponse.builder()
                .id(document.getId().toString())
                .documentType(document.getDocumentType().name())
                .documentNumber(document.getDocumentNumber())
                .nationality(document.getNationality())
                .country(document.getCountry())
                .issuedBy(document.getIssuedBy())
                .issueDate(document.getIssueDate() != null ? document.getIssueDate().toString() : null)
                .expiryDate(document.getExpiryDate() != null ? document.getExpiryDate().toString() : null)
                .isCurrent(document.getIsCurrent())
                .createdAt(document.getCreatedAt())
                .build();
    }

    private TrainingDetailsResponse convertToTrainingDetailsResponse(TrainingDetails details) {
        return TrainingDetailsResponse.builder()
                .id(details.getId().toString())
                .trainingName(details.getTrainingName())
                .certificateName(details.getCertificateName())
                .startDate(details.getStartDate() != null ? details.getStartDate().toString() : null)
                .endDate(details.getEndDate() != null ? details.getEndDate().toString() : null)
                .expiryDate(details.getExpiryDate() != null ? details.getExpiryDate().toString() : null)
                .isCurrent(details.getIsCurrent())
                .createdAt(details.getCreatedAt())
                .build();
    }

    private CompanyInformationResponse convertToCompanyInformationResponse(CompanyInformation company) {
        return CompanyInformationResponse.builder()
                .id(company.getId().toString())
                .companyName(company.getCompanyName())
                .description(company.getDescription())
                .aboutCompany(company.getAboutCompany())
                .overview(company.getOverview())
                .companyType(company.getCompanyType())
                .founded(company.getFounded())
                .revenue(company.getRevenue())
                .website(company.getWebsite())
                .companyEmail(company.getCompanyEmail())
                .contactNumber(company.getContactNumber())
                .address(company.getAddress())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    private DocumentVerificationResponse convertToDocumentVerificationResponse(DocumentVerification verification) {
        return DocumentVerificationResponse.builder()
                .id(verification.getId().toString())
                .employeeName(verification.getEmployeeName())
                .employeeId(verification.getEmployeeId())
                .shareCode(verification.getShareCode())
                .documentType(verification.getDocumentType())
                .issueDate(verification.getIssueDate() != null ? verification.getIssueDate().toString() : null)
                .expiryDate(verification.getExpiryDate() != null ? verification.getExpiryDate().toString() : null)
                .documentUrl(verification.getDocumentUrl())
                .verificationStatus(verification.getVerificationStatus().name())
                .adminNotes(verification.getAdminNotes())
                .createdAt(verification.getCreatedAt())
                .reviewedAt(verification.getReviewedAt())
                .build();
    }
}