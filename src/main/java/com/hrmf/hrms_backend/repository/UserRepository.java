package com.hrmf.hrms_backend.repository;

import com.hrmf.hrms_backend.entity.User;
import com.hrmf.hrms_backend.enums.UserRole;
import com.hrmf.hrms_backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    List<User> findByStatus(UserStatus status);
    Optional<User> findByRefreshToken(String refreshToken);
    long countByStatus(UserStatus status);
    List<User> findByEmailContainingIgnoreCase(String emailPattern);

    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoles(@Param("roles") List<UserRole> roles);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status AND u.role = :role")
    long countByStatusAndRole(@Param("status") UserStatus status, @Param("role") UserRole role);

    Page<User> findByRole(UserRole role, Pageable pageable);

    List<User> findByRole(UserRole role);

    Optional<User> findByIdAndRole(UUID id, UserRole role);

    Optional<User> findByEmailAndRole(String email, UserRole role);

    long countByRole(UserRole role);

    long countByRoleAndStatus(UserRole role, UserStatus status);

}
