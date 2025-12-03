package com.hrmf.user_service.service;

import com.hrmf.user_service.entity.User;
import com.hrmf.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(UUID id) {
        log.info("Fetching user from database: {}", id);
        return userRepository.findById(id);
    }

    @Cacheable(value = "users", key = "#email")
    public Optional<User> findByEmail(String email) {
        log.info("Fetching user by email from database: {}", email);
        return userRepository.findByEmail(email);
    }

    @Caching(
            put = {
                    @CachePut(value = "users", key = "#result.id"),
                    @CachePut(value = "users", key = "#result.email")
            },
            evict = {
                    @CacheEvict(value = "users", key = "'all'")
            }
    )
    public User save(User user) {
        log.info("Saving user: {}", user);
        return userRepository.save(user);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "users", key = "#id"),
                    @CacheEvict(value = "users", key = "#email"),
                    @CacheEvict(value = "users", key = "'all'")
            }
    )
    public void delete(UUID id) {
        log.info("Deleting user from database: {}", id);
        userRepository.deleteById(id);
    }

    @Cacheable(value = "users", key = "'all'")
    public List<User> findAll() {
        log.info("Fetching all users from database");
        return userRepository.findAll();
    }

    @CacheEvict(value = "users", allEntries = true)
    public void clearAllCache() {
        log.info("Clearing all user caches");
    }
}
