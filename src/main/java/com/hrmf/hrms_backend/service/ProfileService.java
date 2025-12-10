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
import org.springframework.web.multipart.MultipartFile;

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
    private final ContactRepository contactInformationRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final EducationDetailsRepository educationDetailsRepository;
    private final JobDetailsRepository jobDetailsRepository;
    private final ServiceDetailsRepository serviceDetailsRepository;
    private final TrainingDetailsRepository trainingDetailsRepository;
    private final CompanyInformationRepository companyInformationRepository;
    private final DocumentVerificationRepository documentVerificationRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public ProfileResponse getCompleteProfile() {
        User user = securityUtil.getCurrentUserOrThrow();

        return ProfileResponse.builder()
                .personalDetails(getPersonalDetailsResponse(user))
                .nationalIdDetails(getNationalIdDetailsResponse(user))
                .contactInformation(getContactInformationResponse(user))
//                .emergencyDetails(getEmergencyDetailsResponse(user))
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
        ContactInformation contactInformation = contactInformationRepository.findFirstByUser(user)
                .orElse(ContactInformation.builder()
                        .user(user)
                        .email(request.getEmail())
                        .city(request.getCity())
                        .postCode(request.getPostCode())
                        .emergencyContact(request.getEmergencyContact())
                        .country(request.getCountry())
                        .mobile(request.getMobile())
                        .address1(request.getAddress1())
                        .address2(request.getAddress2())
                        .address3(request.getAddress3())
                        .build());

        contactInformation.setPostCode(request.getPostCode());
        contactInformation.setAddress1(request.getAddress1());
        contactInformation.setAddress2(request.getAddress2());
        contactInformation.setCity(request.getCity());
        contactInformation.setCountry(request.getCountry());
        contactInformation.setEmergencyContact(request.getEmergencyContact());
        contactInformation.setEmail(request.getEmail());
        contactInformation.setAddress3(request.getAddress3());
        contactInformation.setMobile(request.getMobile());

        ContactInformation saved = contactInformationRepository.save(contactInformation);

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
        ContactInformation contact = contactInformationRepository.findFirstByUser(user)
                .orElseThrow(() -> new CustomException("Contact information not found", HttpStatus.NOT_FOUND));

        return convertToContactInformationResponse(contact);
    }

    @Transactional
    public EmergencyDetailsResponse createOrUpdateEmergencyDetails(UpdateEmergencyDetailsDto request) {
        User user = securityUtil.getCurrentUserOrThrow();

        // Find first emergency contact or create new one
        EmergencyContact emergencyContact = emergencyContactRepository.findByUser(user)
                .orElse(EmergencyContact.builder()
                        .user(user)
                        .build()
                );

        emergencyContact.setEmail(request.getEmail());
        emergencyContact.setAddress(request.getAddress());
        emergencyContact.setName(request.getName());
        emergencyContact.setRelation(request.getRelation());
        emergencyContact.setGiveDetails(request.getGiveDetails());
        emergencyContact.setEmergencyContact(request.getEmergencyContact());
        emergencyContact.setTitleOfCertifiedLicense(request.getTitleOfCertifiedLicense());
        emergencyContact.setLicenseNumber(request.getLicenseNumber());
        emergencyContact.setIssueDate(LocalDate.parse(request.getIssueDate()));
        emergencyContact.setExpiryDate(LocalDate.parse(request.getExpiryDate()));

        EmergencyContact saved = emergencyContactRepository.save(emergencyContact);

        return convertToEmergencyDetailsResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmergencyDetailsResponse getEmergencyDetails() {
        User user = securityUtil.getCurrentUserOrThrow();
        EmergencyContact emergencyContact = emergencyContactRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("Emergency details are not found!", HttpStatus.NOT_FOUND));
        return convertToEmergencyDetailsResponse(emergencyContact);
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
        if (request.getDocuments() != null && request.getDocuments().length > 0) {
            List<String> uploadedFileUrls = uploadPassportDocuments(request.getDocuments(), user);
            Map<String, Object> documentFiles = new HashMap<>();
            documentFiles.put("documents", uploadedFileUrls);
            documentFiles.put("count", uploadedFileUrls.size());
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
        visa.setShareCode(request.getShareCode());
        visa.setImmigrationStatus(request.getImmigrationStatus());
        visa.setCountry(request.getCountryResidency());
        visa.setIssuedBy(request.getIssueBy());
        visa.setIssueDate(request.getIssueDate());
        visa.setExpiryDate(request.getExpDate());
        visa.setIsCurrent(request.getIsCurrentVisa());
        visa.setCountryResidency(request.getCountryResidency());

        // Handle document files if needed
        if (request.getDocuments() != null && request.getDocuments().length > 0) {
            List<String> uploadedFileUrls = uploadVisaDocuments(request.getDocuments(), user);
            Map<String, Object> documentFiles = new HashMap<>();
            documentFiles.put("documents", uploadedFileUrls);
            documentFiles.put("count", uploadedFileUrls.size());
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

        // Check for existing verifications in restricted statuses
        checkIfVerificationExists(user, VerificationStatus.PENDING,
                "Your right to work data already exists with pending status. Please wait for review.");

        checkIfVerificationExists(user, VerificationStatus.APPROVED,
                "Your right to work data already exists with approved status.");

        checkIfVerificationExists(user, VerificationStatus.UNDER_REVIEW,
                "Your right to work data already exists with under review status. Please wait for the result.");

        String folderPath = "document-verifications/" + user.getId();
        String fileName = fileStorageService.storeFile(request.getDocumentFile(), folderPath);

        // Generate file URL
        String fileUrl = fileStorageService.getFileUrl(fileName, folderPath);

        DocumentVerification documentVerification = DocumentVerification.builder()
                .user(user)
                .employeeName(request.getEmployeeName())
                .employeeId(request.getEmployeeId())
                .shareCode(request.getShareCode())
                .documentType(request.getDocumentType())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .documentUrl(fileUrl)
                .documentFileName(fileName)
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

    private void checkIfVerificationExists(User user, VerificationStatus status, String errorMessage) {
        boolean exists = documentVerificationRepository
                .findFirstByUserAndVerificationStatus(user, status)
                .isPresent();

        if (exists) {
            throw new CustomException(errorMessage, HttpStatus.ALREADY_REPORTED);
        }
    }

    private PersonalDetailsResponse getPersonalDetailsResponse(User user) {
        return personalDetailsRepository.findByUser(user)
                .map(this::convertToPersonalDetailsResponse)
                .orElse(null);
    }

    private List<String> uploadPassportDocuments(MultipartFile[] files, User user) {
        List<String> fileUrls = new ArrayList<>();
        String folderPath = "passports/" + user.getId();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            try {
                // Validate file
                validateDocumentFile(file, "Passport document");

                // Store file
                String fileName = fileStorageService.storeFile(file, folderPath);
                String fileUrl = fileStorageService.getFileUrl(fileName, folderPath);
                fileUrls.add(fileUrl);

                log.debug("Uploaded passport document {} for user {}: {}",
                        i + 1, user.getEmail(), fileName);

            } catch (Exception e) {
                log.error("Failed to upload passport document {} for user {}: {}",
                        i + 1, user.getEmail(), e.getMessage());
                throw new CustomException(
                        "Failed to upload passport document " + (i + 1) + ": " + e.getMessage(),
                        HttpStatus.BAD_REQUEST);
            }
        }

        return fileUrls;
    }

    private List<String> uploadVisaDocuments(MultipartFile[] files, User user) {
        List<String> fileUrls = new ArrayList<>();
        String folderPath = "visas/" + user.getId();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            try {
                // Validate file
                validateDocumentFile(file, "Visa document");

                // Store file
                String fileName = fileStorageService.storeFile(file, folderPath);
                String fileUrl = fileStorageService.getFileUrl(fileName, folderPath);
                fileUrls.add(fileUrl);

                log.debug("Uploaded visa document {} for user {}: {}",
                        i + 1, user.getEmail(), fileName);

            } catch (Exception e) {
                log.error("Failed to upload visa document {} for user {}: {}",
                        i + 1, user.getEmail(), e.getMessage());
                throw new CustomException(
                        "Failed to upload visa document " + (i + 1) + ": " + e.getMessage(),
                        HttpStatus.BAD_REQUEST);
            }
        }

        return fileUrls;
    }

    private void validateDocumentFile(MultipartFile file, String documentType) {
        if (file.isEmpty()) {
            throw new CustomException(documentType + " file is empty", HttpStatus.BAD_REQUEST);
        }

        // Check file size (max 5MB per document)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new CustomException(
                    documentType + " file size exceeds maximum limit of 5MB",
                    HttpStatus.BAD_REQUEST);
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") &&
                        !contentType.equals("application/pdf") &&
                        !contentType.equals("application/msword") &&
                        !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new CustomException(
                    documentType + " file type not allowed. Allowed types: Images, PDF, Word documents",
                    HttpStatus.BAD_REQUEST);
        }
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
        return contactInformationRepository.findFirstByUser(user)
                .map(this::convertToContactInformationResponse)
                .orElse(null);
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

    private ContactInformationResponse convertToContactInformationResponse(ContactInformation address) {
        return ContactInformationResponse.builder()
                .id(address.getId().toString())
                .postCode(address.getPostCode())
                .address1(address.getAddress1())
                .address2(address.getAddress2())
                .address3(address.getAddress3())
                .city(address.getCity())
                .country(address.getCountry())
                .mobile(address.getMobile())
                .emergencyContact(address.getEmergencyContact())
                .email(address.getEmail())
                .build();
    }

    private EmergencyDetailsResponse convertToEmergencyDetailsResponse(EmergencyContact contact) {
        return EmergencyDetailsResponse.builder()
                .id(contact.getId().toString())
                .name(contact.getName())
                .relation(contact.getRelation())
                .giveDetails(contact.getGiveDetails())
                .email(contact.getEmail())
                .emergencyContact(contact.getEmergencyContact())
                .address(contact.getAddress())
                .titleOfCertifiedLicense(contact.getTitleOfCertifiedLicense())
                .licenseNumber(contact.getLicenseNumber())
                .issueDate(contact.getIssueDate().toString())
                .expiryDate(contact.getExpiryDate().toString())
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
                .build();
    }

    private IdentityDocumentResponse convertToIdentityDocumentResponse(IdentityDocument document) {
        List<String> documentUrls = new ArrayList<>();
        Integer documentCount = 0;

        // Extract document URLs from documentFiles map
        if (document.getDocumentFiles() != null) {
            Object docs = document.getDocumentFiles().get("documents");
            if (docs instanceof List) {
                documentUrls = (List<String>) docs;
                documentCount = documentUrls.size();
            }
        }

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
                .shareCode(document.getShareCode())
                .immigrationStatus(document.getImmigrationStatus())
                .countryResidency(document.getCountryResidency())
                .documentUrls(documentUrls)
                .documentCount(documentCount)
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