package com.aha.domain.pastpaper.entity;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.pastpaper.enums.PastPaperStatus;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "past_paper",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_past_paper_exam_version_year_round_no",
        columnNames = {"exam_version_id", "year", "round_no"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PastPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_version_id", nullable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_past_paper_exam_version_id"))
    private ExamVersion examVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PastPaperStatus status;

    @PositiveOrZero
    @Column(name = "total_item_count", nullable = false)
    private int totalItemCount;

    @Column(name = "is_reviewed", nullable = false)
    private boolean isReviewed = false;

    @Min(2013)
    @Column(name = "year", nullable = false)
    private int year;

    @Positive
    @Column(name = "round_no", nullable = false)
    private int roundNo;

    @Positive
    @Column(name = "time_limit", nullable = false)
    private int timeLimit;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void validatePublished() {
        if (status != PastPaperStatus.PUBLISHED){
            throw new BusinessException(ErrorCode.PAST_PAPER_NOT_PUBLISHED);
        }
    }

    public void validateCanSolve() {

        examVersion.validateActive();
        examVersion.getExam().validateActive();
        validatePublished();
    }

    public String createTitle() {
        return "%s %d년 %d회차 시험".formatted(examVersion.getExam().getName(), year, roundNo);
    }
}
