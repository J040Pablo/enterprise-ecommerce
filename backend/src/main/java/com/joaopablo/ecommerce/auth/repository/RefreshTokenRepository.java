package com.joaopablo.ecommerce.auth.repository;

import com.joaopablo.ecommerce.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("""
            SELECT rt FROM RefreshToken rt
            JOIN FETCH rt.user u
            LEFT JOIN FETCH u.userRoles ur
            LEFT JOIN FETCH ur.role
            WHERE rt.token = :token
            """)
    Optional<RefreshToken> findByTokenWithUser(String token);

    Optional<RefreshToken> findByToken(String token);
}
