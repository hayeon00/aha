package com.aha.domain.pastpaper.scheduler;

import com.aha.domain.pastpaper.repository.PastPaperAttemptRepository;
import com.aha.domain.pastpaper.service.PastPaperAttemptService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PastPaperAttemptExpirationScheduler {

    private static final int BATCH_SIZE = 100;

    private final PastPaperAttemptRepository attemptRepository;
    private final PastPaperAttemptService attemptService;

    @Scheduled(
        fixedDelayString =
            "${scheduler.past-paper-attempt-expiration.fixed-delay}"
    )
    public void gradeExpiredAttempts() {
        List<Long> attemptIds =
            attemptRepository
                .findExpiredSolvingAttemptIds(
                    PageRequest.of(0, BATCH_SIZE)
                );

        for (Long attemptId : attemptIds) {
            try {
                attemptService.submitExpiredAttempt(
                    attemptId
                );
            } catch (Exception exception) {
                log.error(
                    "만료 풀이 자동 채점 실패. attemptId={}",
                    attemptId,
                    exception
                );
            }
        }
    }
}