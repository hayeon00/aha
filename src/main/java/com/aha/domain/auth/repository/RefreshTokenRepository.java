package com.aha.domain.auth.repository;

import com.aha.domain.auth.entity.RefreshToken;
import com.aha.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser_Id(Long userId);
}