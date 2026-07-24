package com.aha.domain.pastpaper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Table(
    name = "user_answer",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_answer_attempt_problem",
        columnNames = {"past_paper_attempt_id", "problem_id"}
    )
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "past_paper_attempt_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_user_answer_past_paper_attempt_id"))
    private  PastPaperAttempt pastPaperAttempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_user_answer_problem_id"))
    private Problem problem;

    @Column(name = "user_answer", length = 500)
    private String userAnswer;

    @Column(name = "correct")
    private Boolean correct;

    @Column(name = "marked_for_review", nullable = false)
    private boolean markedForReview;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public static UserAnswer create(PastPaperAttempt pastPaperAttempt, Problem problem,
        String userAnswer,boolean markedForReviewed) {
        return UserAnswer.builder()
            .pastPaperAttempt(pastPaperAttempt)
            .problem(problem)
            .userAnswer(userAnswer != null ? userAnswer.strip() : null)
            .markedForReview(markedForReviewed)
            .build();
    }

    public void update(String userAnswer) {
        this.userAnswer = userAnswer.strip();
    }

    public void update() {
        markedForReview = !markedForReview;
    }

    public void updateUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer.strip();
    }

    public void toggleChecked() {
        markedForReview = !markedForReview;
    }

    public void right() {
        correct = true;
    }

    public void wrong() {
        correct = false;
    }

    public void requestGrading() {
        problem.grade(this);
    }
}
