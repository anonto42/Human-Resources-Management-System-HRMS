package com.hrmf.hrms_backend.util;

import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(username);
        } else if (principal instanceof String) {
            // When principal is just a string (username)
            String username = (String) principal;
            return userRepository.findByEmail(username);
        }

        return Optional.empty();
    }

    public User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));
    }

    public Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    public UUID getCurrentUserIdOrThrow() {
        return getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));
    }

    public Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return Optional.of(((UserDetails) principal).getUsername());
        } else if (principal instanceof String) {
            return Optional.of((String) principal);
        }

        return Optional.empty();
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    public boolean isOwner(UUID userId) {
        return getCurrentUserId()
                .map(id -> id.equals(userId))
                .orElse(false);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }

    public boolean isEmployer() {
        return hasRole("EMPLOYER") || isAdmin();
    }
}