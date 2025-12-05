package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.dto.user.CreateUserDto;
import com.hrmf.hrms_backend.dto.user.UpdateUserDto;
import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.enums.UserStatus;
import com.hrmf.hrms_backend.exception.DuplicateResourceException;
import com.hrmf.hrms_backend.exception.ResourceNotFoundException;
import com.hrmf.hrms_backend.exception.ValidationException;
import com.hrmf.hrms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(CreateUserDto userDto) {
        log.info("Creating new user with email: {}", userDto.getEmail());

        validateCreateUserDto(userDto);

        // Check if email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("User creation failed - Email already exists: {}", userDto.getEmail());
            throw new DuplicateResourceException("Email already exists!");
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail().toLowerCase().trim());
        user.setHashPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(userDto.getRole() != null ? userDto.getRole() : UserRole.EMPLOYEE);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return savedUser;
    }

    public User findByEmail(String email) {
        log.debug("Finding user by email: {}", email);

        if (!StringUtils.hasText(email)) {
            throw new ValidationException("Email cannot be empty");
        }

        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
    }

    public User findById(UUID id) {
        log.debug("Finding user by ID: {}", id);

        if (id == null) {
            throw new ValidationException("User ID cannot be null");
        }

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });
    }

    @Transactional
    public User updateUser(UUID id, UpdateUserDto updateDto) {
        log.info("Updating user with ID: {}", id);

        User user = findById(id);

        // Update email if provided and changed
        if (StringUtils.hasText(updateDto.getEmail()) &&
                !user.getEmail().equalsIgnoreCase(updateDto.getEmail().trim())) {

            String newEmail = updateDto.getEmail().toLowerCase().trim();
            if (userRepository.existsByEmailAndIdNot(newEmail, id)) {
                log.warn("Email update failed - Email already exists: {}", newEmail);
                throw new DuplicateResourceException("Email already exists!");
            }
            user.setEmail(newEmail);
        }

        // Update password if provided
        if (StringUtils.hasText(updateDto.getPassword())) {
            user.setHashPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        // Update role if provided
        if (updateDto.getRole() != null) {
            user.setRole(updateDto.getRole());
        }

        // Update status if provided
        if (updateDto.getStatus() != null) {
            user.setStatus(updateDto.getStatus());
        }

        // Update image URL if provided
        if (updateDto.getImageUrl() != null) {
            user.setImageUrl(updateDto.getImageUrl());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", id);

        return updatedUser;
    }

    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);

        User user = findById(id);

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        // Or hard delete (uncomment if needed):
        // userRepository.delete(user);

        log.info("User marked as inactive with ID: {}", id);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        return userRepository.findAll(pageable);
    }

    public List<User> getUsersByRole(UserRole role) {
        log.debug("Fetching users by role: {}", role);

        if (role == null) {
            throw new ValidationException("Role cannot be null");
        }

        return userRepository.findByRole(role);
    }

    public List<User> getUsersByStatus(UserStatus status) {
        log.debug("Fetching users by status: {}", status);

        if (status == null) {
            throw new ValidationException("Status cannot be null");
        }

        return userRepository.findByStatus(status);
    }

    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    @Transactional
    public void updateRefreshToken(UUID userId, String refreshToken) {
        log.debug("Updating refresh token for user ID: {}", userId);

        User user = findById(userId);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }

    public Optional<User> findByRefreshToken(String refreshToken) {
        log.debug("Finding user by refresh token");

        if (!StringUtils.hasText(refreshToken)) {
            return Optional.empty();
        }

        return userRepository.findByRefreshToken(refreshToken);
    }

    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        log.info("Changing password for user ID: {}", userId);

        User user = findById(userId);

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getHashPassword())) {
            log.warn("Password change failed - Invalid old password for user ID: {}", userId);
            throw new ValidationException("Invalid old password");
        }

        // Set new password
        user.setHashPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", userId);
    }

    private void validateCreateUserDto(CreateUserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("User data cannot be null");
        }

        if (!StringUtils.hasText(userDto.getEmail())) {
            throw new ValidationException("Email cannot be empty");
        }

        if (!StringUtils.hasText(userDto.getPassword())) {
            throw new ValidationException("Password cannot be empty");
        }

        if (userDto.getPassword().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
    }

    public long countByStatus(UserStatus status) {
        if (status == null) {
            throw new ValidationException("Status cannot be null");
        }
        return userRepository.countByStatus(status);
    }

    public List<User> searchByEmail(String emailPattern) {
        if (!StringUtils.hasText(emailPattern)) {
            throw new ValidationException("Search pattern cannot be empty");
        }

        return userRepository.findByEmailContainingIgnoreCase(emailPattern.trim());
    }

    public List<User> getActiveUsers() {
        return userRepository.findByStatus(UserStatus.ACTIVE);
    }
}