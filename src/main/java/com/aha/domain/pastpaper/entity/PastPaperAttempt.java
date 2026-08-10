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

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static PastPaperAttempt create(
        long userId,
        PastPaper paper,
        LocalDateTime dueAt
    ) {

        return PastPaperAttempt.builder()
            .userId(userId)
            .pastPaper(paper)
            .status(PastPaperAttemptStatus.SOLVING)
            .dueAt(dueAt)
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

        if (!now.isBefore(dueAt)) {
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

    public void updateAfterGraded(
        TotalResultAggregator result,
        LocalDateTime completedAt
    ) {
        status = PastPaperAttemptStatus.GRADED;
        userScore = result.getUserScore();
        passed = result.isPassed();

        LocalDateTime effectiveCompletedAt =
            completedAt.isBefore(dueAt)
                ? completedAt
                : dueAt;

        elapsedTime = Math.toIntExact(
            Duration.between(
                startedAt,
                effectiveCompletedAt
            ).getSeconds()
        );
    }

    public void validateGraded() {

        if(status != PastPaperAttemptStatus.GRADED){
            throw new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_GRADED);
        }
    }

    public void validateCanSeeResult(Long userId){

        validateOwner(userId);
        validateGraded();
    }

    public boolean isExpiredSolving(LocalDateTime now) {
        return status == PastPaperAttemptStatus.SOLVING
            && !now.isBefore(dueAt);
    }
}
