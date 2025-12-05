package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.dto.employer.AddEmployeeRequestDto;
import com.hrmf.hrms_backend.dto.employer.AddEmployeeResponseDto;
import com.hrmf.hrms_backend.dto.user.CreateUserDto;
import com.hrmf.hrms_backend.entity.Employee;
import com.hrmf.hrms_backend.entity.PersonalDetails;
import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.enums.Gender;
import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.repository.EmployeeRepository;
import com.hrmf.hrms_backend.repository.PersonalDetailsRepository;
import com.hrmf.hrms_backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerService {

    private final UserService userService;
    private final EmployeeRepository employeeRepository;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public AddEmployeeResponseDto addEmployee(AddEmployeeRequestDto addEmployeeRequestDto) {

        if (!securityUtil.isEmployer()) {
            throw new AccessDeniedException("You don't have permission to add employees");
        }

        User currentEmployer = securityUtil.getCurrentUserOrThrow();

        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName(addEmployeeRequestDto.getName());
        createUserDto.setEmail(addEmployeeRequestDto.getEmail());
        createUserDto.setPassword(addEmployeeRequestDto.getPassword());
        createUserDto.setRole(UserRole.EMPLOYEE);

        User employeeUser = userService.createUser(createUserDto);

        Employee employee = new Employee();
        employee.setUser(employeeUser);
        employee.setEmployer(currentEmployer);
        employee.setEmployeeRole(addEmployeeRequestDto.getEmployeeRole());
        employee.setNumber(addEmployeeRequestDto.getNumber());
        employee.setShift(addEmployeeRequestDto.getShift());
        employee.setOfficeTime(addEmployeeRequestDto.getOfficeTime());

        Employee savedEmployee = employeeRepository.save(employee);

        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setUser(employeeUser);
        personalDetails.setDateOfBirth(addEmployeeRequestDto.getBirthDate());
        personalDetails.setGender(Gender.valueOf(addEmployeeRequestDto.getGender()));

        personalDetailsRepository.save(personalDetails);

        return AddEmployeeResponseDto.builder()
                .id(savedEmployee.getId().toString())
                .name(addEmployeeRequestDto.getName())
                .email(addEmployeeRequestDto.getEmail())
                .gender(addEmployeeRequestDto.getGender())
                .role(UserRole.valueOf(employeeUser.getRole().toString()))
                .build();
    }
}