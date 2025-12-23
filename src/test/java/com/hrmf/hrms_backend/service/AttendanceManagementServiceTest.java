package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.document.Attendance;
import com.hrmf.hrms_backend.repository.EmployeeRepository;
import com.hrmf.hrms_backend.repository.mongo.AttendanceRepository;
import com.hrmf.hrms_backend.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("Attendance Services Tests")
class AttendanceManagementServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AttendanceManagementService attendanceManagementService;

    private Attendance testAttendance;

    @BeforeEach
    void setUp() {
//        final User testUser = User.

//        this.testAttendance = Attendance.builder()
//                .id(String.valueOf(UUID.randomUUID()))
//                .userId()
//                .employerId()
//                .employeeName()
//                .employeeEmail()
//                .attendanceDate(LocalDate.now())
//                .status(AttendanceStatus.CHECKED_OUT)
//                .build();
    }

    @Nested
    @DisplayName("Attendance Service Tests")
    class AttendanceServiceTest {

        @Test
        @DisplayName("Create Attendance")
        void createAttendanceSuccessfully() {

        }
    }

}