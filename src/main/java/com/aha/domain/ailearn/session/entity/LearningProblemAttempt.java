package com.aha.domain.ailearn.session.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "learning_problem_attempt")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningProblemAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "learning_session_id")
    private Long learningSessionId;

    @Column(name = "exam_scope_node_id", nullable = false)
    private Long examScopeNodeId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "selected_choice_id")
    private Long selectedChoiceId;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LearningProblemAttempt(
            Long userId,
            Long learningSessionId,
            Long examScopeNodeId,
            Long problemId,
            Long selectedChoiceId,
            boolean correct
    ) {
        this.userId = userId;
        this.learningSessionId = learningSessionId;
        this.examScopeNodeId = examScopeNodeId;
        this.problemId = problemId;
        this.selectedChoiceId = selectedChoiceId;
        this.correct = correct;
        this.submittedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (this.submittedAt == null) {
            this.submittedAt = now;
        }

        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}