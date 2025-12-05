package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.dto.superAdmin.CreateSubAdminRequestDto;
import com.hrmf.hrms_backend.dto.superAdmin.CreateSubAdminResponseDto;
import com.hrmf.hrms_backend.dto.user.CreateUserDto;
import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final UserService userService;
    private final UserRepository userRepository;

    public CreateSubAdminResponseDto createSuperAdmin(
            CreateSubAdminRequestDto createSubAdminRequestDto
    ) {

        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setEmail(createSubAdminRequestDto.getEmail());
        createUserDto.setPassword(createSubAdminRequestDto.getPassword());
        createUserDto.setName(createSubAdminRequestDto.getName());

        User user = userService.createUser(createUserDto);

        return CreateSubAdminResponseDto.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .build();

    }
}
