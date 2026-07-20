package com.aha.domain.auth.oauth2.repository;

import com.aha.domain.auth.oauth2.entity.OAuthAuthorizationCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OAuthAuthorizationCodeRepository
        extends JpaRepository<OAuthAuthorizationCode, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select code
            from OAuthAuthorizationCode code
            join fetch code.user
            where code.codeHash = :codeHash
            """)
    Optional<OAuthAuthorizationCode> findByCodeHashForUpdate(
            @Param("codeHash") String codeHash
    );

    @Modifying
    @Query("""
        delete from OAuthAuthorizationCode code
        where code.expiresAt < :now
           or code.usedAt < :usedBefore
        """)
    int deleteExpiredOrUsedCodes(
            @Param("now") LocalDateTime now,
            @Param("usedBefore") LocalDateTime usedBefore
    );
}