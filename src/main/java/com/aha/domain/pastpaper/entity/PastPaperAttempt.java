package com.aha.domain.pastpaper.entity;

import com.aha.domain.pastpaper.aggregation.TotalResultAggregator;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Table(
    name = "past_paper_attempt"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class PastPaperAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "past_paper_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_past_paper_attempt_past_paper_id"))
    private PastPaper pastPaper;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PastPaperAttemptStatus status;

    @PositiveOrZero
    @Column(name = "user_score")
    private Integer userScore;

    @Column(name = "passed")
    private Boolean passed;

    @PositiveOrZero
    @Column(name = "elapsed_time")
    private Integer elapsedTime;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static PastPaperAttempt create(Long userId, PastPaper pastPaper) {

        return PastPaperAttempt.builder()
            .userId(userId)
            .pastPaper(pastPaper)
            .status(PastPaperAttemptStatus.SOLVING)
            .build();
    }

    public void validateOwner(Long userId) {
        if (!Objects.equals(this.userId, userId)) {
            throw new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_YOURS);
        }
    }

    private void validateSolving() {
        if (status != PastPaperAttemptStatus.SOLVING) {
            throw new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_SOLVING);
        }
    }

    private void validateWithinSolvingTime(LocalDateTime now) {
        LocalDateTime deadLine = startedAt.plusSeconds(pastPaper.getTimeLimit());
        if (!now.isBefore(deadLine)) {
            throw new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_TIME_EXPIRED);
        }
    }

    public void validateCanSolve(Long userId) {
        validateOwner(userId);
        validateSolving();
        validateWithinSolvingTime(LocalDateTime.now());
    }

    public void validateCanSubmit(Long userId) {
        validateOwner(userId);
        validateSolving();
    }

    public void updateAfterGraded(TotalResultAggregator totalAggregator) {
        status = PastPaperAttemptStatus.GRADED;
        userScore = totalAggregator.getUserScore();
        passed = totalAggregator.isPassed();
        LocalDateTime now = LocalDateTime.now();
        elapsedTime = (int) Duration.between(startedAt, now).getSeconds();
    }
}
