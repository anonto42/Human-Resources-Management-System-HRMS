package com.hrmf.hrms_backend.controller;

import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Only Super Admin can delete users
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // Admin and above can update user roles
    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<User> updateUserRole(
            @PathVariable UUID id,
            @RequestParam String role) {
        // Implementation
        return ResponseEntity.ok().build();
    }

    // Any authenticated user can view their own profile
    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile() {
        // Get current user from security context
        // Implementation
        return ResponseEntity.ok().build();
    }

}
