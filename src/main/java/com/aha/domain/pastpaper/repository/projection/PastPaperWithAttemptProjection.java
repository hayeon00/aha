package com.aha.domain.pastpaper.repository.projection;

import com.aha.domain.pastpaper.entity.PastPaper;
import java.time.LocalDateTime;

public interface PastPaperWithAttemptProjection {

    PastPaper getPastPaper();

    Long getSolvingAttemptId();

    LocalDateTime getAttemptStartedAt();
}
