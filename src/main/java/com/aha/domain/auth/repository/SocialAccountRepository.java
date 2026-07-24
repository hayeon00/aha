package com.aha.domain.auth.repository;

import com.aha.domain.auth.entity.SocialAccount;
import com.aha.domain.auth.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository
        extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount>
    findByProviderAndProviderId(
            SocialProvider provider,
            String providerId
    );

    Optional<SocialAccount>
    findByUser_IdAndProvider(
            Long userId,
            SocialProvider provider
    );

    boolean existsByProviderAndProviderId(
            SocialProvider provider,
            String providerId
    );
}