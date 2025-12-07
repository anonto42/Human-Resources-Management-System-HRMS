package com.hrmf.hrms_backend.dto.subAdmin;

import com.hrmf.hrms_backend.dto.user.AddressDto;
import com.hrmf.hrms_backend.dto.user.EmergencyContactDto;
import com.hrmf.hrms_backend.dto.user.IdentityDocumentDto;
import com.hrmf.hrms_backend.dto.user.PersonalDetailsDto;
import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDto {
    private UUID id;
    private String name;
    private String email;
    private String imageUrl;
    private UserRole role;
    private UserStatus status;
    private PersonalDetailsDto personalDetails;
    private List<AddressDto> addresses;
    private List<EmergencyContactDto> emergencyContacts;
    private List<IdentityDocumentDto> identityDocuments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}