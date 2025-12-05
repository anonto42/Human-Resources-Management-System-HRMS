package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.dto.auth.SignInRequestDto;
import com.hrmf.hrms_backend.dto.auth.SignInResponseDto;
import com.hrmf.hrms_backend.dto.employer.AddEmployeeResponseDto;
import com.hrmf.hrms_backend.dto.user.CreateUserDto;
import com.hrmf.hrms_backend.entity.CustomUserDetails;
import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.enums.UserStatus;
import com.hrmf.hrms_backend.exception.BusinessException;
import com.hrmf.hrms_backend.repository.EmployeeRepository;
import com.hrmf.hrms_backend.repository.PersonalDetailsRepository;
import com.hrmf.hrms_backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final SecurityUtil securityUtil;

    public SignInResponseDto signIn(SignInRequestDto data) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Check user status
        if (user.getStatus().equals(UserStatus.DELETED) || user.getStatus().equals(UserStatus.BLOCKED)) {
            throw BusinessException.forbidden("User is { " + user.getStatus().toString() + " }!");
        }

        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Update refresh token in database
        userService.updateRefreshToken(user.getId(), refreshToken);

        // Prepare user info
        Map<String, String> responseUser = new HashMap<>();
        responseUser.put("id", user.getId().toString());
        responseUser.put("email", user.getEmail());
        responseUser.put("name", user.getName());
        responseUser.put("role", user.getRole().name());
        responseUser.put("status", user.getStatus().name());

        return SignInResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(responseUser)
                .build();
    }

    @Transactional
    public AddEmployeeResponseDto createEmployer(CreateUserDto addEmployeeRequestDto) {

        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName(addEmployeeRequestDto.getName());
        createUserDto.setEmail(addEmployeeRequestDto.getEmail());
        createUserDto.setPassword(addEmployeeRequestDto.getPassword());
        createUserDto.setRole(UserRole.EMPLOYER);

        User employer = userService.createUser(createUserDto);

        return AddEmployeeResponseDto.builder()
                .name(employer.getName())
                .email(employer.getEmail())
                .role(UserRole.valueOf(employer.getRole().toString()))
                .build();
    }
}