package com.aha.domain.auth.oauth2.service;

import com.aha.domain.auth.oauth2.repository.OAuthAuthorizationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuthAuthorizationCodeCleanupScheduler {

    private final OAuthAuthorizationCodeRepository repository;

    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();

        repository.deleteExpiredOrUsedCodes(
                now,
                now.minusMinutes(10)
        );
    }
}