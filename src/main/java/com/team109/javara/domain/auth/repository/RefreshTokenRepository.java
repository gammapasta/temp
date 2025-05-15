package com.team109.javara.domain.auth.repository;

import com.team109.javara.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUsername(String username);
    void deleteByUsername(String username);
    boolean existsByUsername(String username);
}