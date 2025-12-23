package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.config.AdminProperties;
import com.hrmf.hrms_backend.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin Initializer Test")
class AdminInitializerTest {

    @Mock
    private AdminProperties adminProperties;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminInitializer adminInitializer;

    @Nested
    @DisplayName("Initialize Admin Tests")
    class InitializeAdminTest {

        @Test
        @DisplayName("Check if super admin is not exists then create admin")
        void TestTheAdminInitializer() {
            // Given

            // When
            adminInitializer.initializeSuperAdmin();

            // Then
//            Assertions.assertNotNull(!false || null);
        }
    }
}