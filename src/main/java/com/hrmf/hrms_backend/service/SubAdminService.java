package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.dto.subAdmin.*;
import com.hrmf.hrms_backend.dto.user.*;
import com.hrmf.hrms_backend.entity.*;
import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.enums.UserStatus;
import com.hrmf.hrms_backend.exception.ResourceNotFoundException;
import com.hrmf.hrms_backend.repository.*;
import com.hrmf.hrms_backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubAdminService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final AddressRepository addressRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final IdentityDocumentRepository identityDocumentRepository;
    private final EmployerRepository employerRepository;
    private final SecurityUtil securityUtil;

    // Get all users with basic info
    public List<UserListDto> getAllUsers(UserRole roleFilter, UserStatus statusFilter, String search) {
        List<User> users;

        if (search != null && !search.trim().isEmpty()) {
            // Search by name or email
            users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search.trim(), search.trim()
            );
        } else {
            // Get all users or filter by role/status
            users = userRepository.findAll();
        }

        // Apply filters
        return users.stream()
                .filter(user -> roleFilter == null || user.getRole() == roleFilter)
                .filter(user -> statusFilter == null || user.getStatus() == statusFilter)
                .map(this::convertToUserListDto)
                .collect(Collectors.toList());
    }

    // Get paginated users
    public Page<UserListDto> getUsersPaginated(int page, int size, UserRole roleFilter, UserStatus statusFilter, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> usersPage;
        if (search != null && !search.trim().isEmpty()) {
            usersPage = userRepository.findBySearchTerm(search.trim(), roleFilter, statusFilter, pageable);
        } else if (roleFilter != null && statusFilter != null) {
            usersPage = userRepository.findByRoleAndStatus(roleFilter, statusFilter, pageable);
        } else if (roleFilter != null) {
            usersPage = userRepository.findByRole(roleFilter, pageable);
        } else if (statusFilter != null) {
            usersPage = (Page<User>) userRepository.findByStatus(statusFilter, pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        return usersPage.map(this::convertToUserListDto);
    }

    // Get users by role
    public List<UserListDto> getUsersByRole(UserRole role) {
        if (role != UserRole.EMPLOYEE && role != UserRole.EMPLOYER) {
            throw new IllegalArgumentException("Role must be either EMPLOYEE or EMPLOYER");
        }

        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(this::convertToUserListDto)
                .collect(Collectors.toList());
    }

    // Get single user with all details
    public UserDetailDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return convertToUserDetailDto(user);
    }

    // Get user by email
    public UserDetailDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return convertToUserDetailDto(user);
    }

    // Block user
    @Transactional
    public void blockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException("User is already blocked");
        }

        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        log.info("User blocked: {} ({})", user.getEmail(), user.getId());
    }

    // Unblock user
    @Transactional
    public void unblockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() != UserStatus.BLOCKED) {
            throw new IllegalStateException("User is not blocked");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("User unblocked: {} ({})", user.getEmail(), user.getId());
    }

    // Delete user
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("User deleted (soft): {} ({})", user.getEmail(), user.getId());
    }

    // Get dashboard statistics
    public DashboardStatsDto getDashboardStats() {
        Long totalEmployees = userRepository.countByRole(UserRole.EMPLOYEE);
        Long activeEmployees = userRepository.countByRoleAndStatus(UserRole.EMPLOYEE, UserStatus.ACTIVE);
        Long inactiveEmployees = totalEmployees - activeEmployees;

        Long totalEmployers = userRepository.countByRole(UserRole.EMPLOYER);
        Long activeEmployers = userRepository.countByRoleAndStatus(UserRole.EMPLOYER, UserStatus.ACTIVE);
        Long inactiveEmployers = totalEmployers - activeEmployers;

        Long totalUsers = userRepository.count();
        Long totalActiveUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        Long totalInactiveUsers = totalUsers - totalActiveUsers;

        return DashboardStatsDto.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .inactiveEmployees(inactiveEmployees)
                .totalEmployers(totalEmployers)
                .activeEmployers(activeEmployers)
                .inactiveEmployers(inactiveEmployers)
                .totalUsers(totalUsers)
                .totalActiveUsers(totalActiveUsers)
                .totalInactiveUsers(totalInactiveUsers)
                .build();
    }

    // Create employer
    @Transactional
    public UserDetailDto createEmployer(CreateEmployerRequestDto request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        User currentUser = securityUtil.getCurrentUserOrThrow();

        // Create user
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName(request.getName());
        createUserDto.setEmail(request.getEmail());
        createUserDto.setPassword(request.getPassword());
        createUserDto.setRole(UserRole.EMPLOYER);

        User user = userService.createUser(createUserDto);

        // Create personal details if provided
        if (request.getContactNumber() != null || request.getCompanyName() != null || request.getDesignation() != null) {
            PersonalDetails personalDetails = new PersonalDetails();
            personalDetails.setUser(user);
            personalDetails.setMobile(request.getContactNumber());

            // Store company name and designation in about field or create new fields
            StringBuilder aboutBuilder = new StringBuilder();
            if (request.getCompanyName() != null) {
                aboutBuilder.append("Company: ").append(request.getCompanyName());
            }
            if (request.getDesignation() != null) {
                if (aboutBuilder.length() > 0) aboutBuilder.append(" | ");
                aboutBuilder.append("Designation: ").append(request.getDesignation());
            }
            if (aboutBuilder.length() > 0) {
                personalDetails.setAbout(aboutBuilder.toString());
            }

            personalDetailsRepository.save(personalDetails);
            user.setPersonalDetails(personalDetails);
        }

        // Create employer record
        Employer employer = new Employer();
        employer.setUser(user);
        employer.setCreatedBy(currentUser);
        employer.setShift(request.getWorkingTime());
        employer.setNumber(request.getContactNumber());
        employer.setOfficeTime(request.getWorkingTime().toString());
        employerRepository.save(employer);

        log.info("Employer created by sub-admin: {} ({})", user.getEmail(), user.getId());

        return convertToUserDetailDto(user);
    }

    // Convert User to UserListDto
    private UserListDto convertToUserListDto(User user) {
        String designation = "";
        String contactNumber = "";
        String companyName = "";

        // Extract additional info from personal details
        if (user.getPersonalDetails() != null) {
            contactNumber = user.getPersonalDetails().getMobile() != null ?
                    user.getPersonalDetails().getMobile() : "";

            // Parse about field for company and designation
            if (user.getPersonalDetails().getAbout() != null) {
                String about = user.getPersonalDetails().getAbout();
                if (about.contains("Company:") && about.contains("Designation:")) {
                    String[] parts = about.split(" \\| ");
                    for (String part : parts) {
                        if (part.startsWith("Company:")) {
                            companyName = part.replace("Company:", "").trim();
                        } else if (part.startsWith("Designation:")) {
                            designation = part.replace("Designation:", "").trim();
                        }
                    }
                }
            }
        }

        return UserListDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .designation(designation)
                .contactNumber(contactNumber)
                .companyName(companyName)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // Convert User to UserDetailDto
    private UserDetailDto convertToUserDetailDto(User user) {
        // Convert personal details
        PersonalDetailsDto personalDetailsDto = null;
        if (user.getPersonalDetails() != null) {
            personalDetailsDto = PersonalDetailsDto.builder()
                    .about(user.getPersonalDetails().getAbout())
                    .employerCode(user.getPersonalDetails().getEmployerCode())
                    .gender(user.getPersonalDetails().getGender())
                    .dateOfBirth(user.getPersonalDetails().getDateOfBirth())
                    .maritalStatus(user.getPersonalDetails().getMaritalStatus())
                    .mobile(user.getPersonalDetails().getMobile())
                    .emergencyContact(user.getPersonalDetails().getEmergencyContact())
                    .build();
        }

        // Convert addresses
        List<AddressDto> addressDtos = user.getAddresses().stream()
                .map(address -> AddressDto.builder()
                        .id(address.getId())
                        .addressType(address.getAddressType())
                        .postCode(address.getPostCode())
                        .address1(address.getAddress1())
                        .address2(address.getAddress2())
                        .address3(address.getAddress3())
                        .city(address.getCity())
                        .country(address.getCountry())
                        .isPrimary(address.getIsPrimary())
                        .build())
                .collect(Collectors.toList());

        // Convert emergency contacts
        List<EmergencyContactDto> emergencyContactDtos = user.getEmergencyContacts().stream()
                .map(contact -> EmergencyContactDto.builder()
                        .id(contact.getId())
                        .name(contact.getName())
                        .email(contact.getEmail())
                        .address(contact.getAddress())
                        .relationship(contact.getRelation())
                        .contactNumber(contact.getEmergencyContact())
                        .additionalDetails(contact.getAddress())
                        .build())
                .collect(Collectors.toList());

        // Convert identity documents
        List<IdentityDocumentDto> identityDocumentDtos = user.getIdentityDocuments().stream()
                .map(doc -> IdentityDocumentDto.builder()
                        .id(doc.getId())
                        .nationality(doc.getNationality())
                        .issueDate(doc.getIssueDate())
                        .expiryDate(doc.getExpiryDate())
                        .createdAt(doc.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return UserDetailDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .imageUrl(user.getImageUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .personalDetails(personalDetailsDto)
                .addresses(addressDtos)
                .emergencyContacts(emergencyContactDtos)
                .identityDocuments(identityDocumentDtos)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}