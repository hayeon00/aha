package com.aha.domain.ailearn.session.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "learning_progress")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exam_scope_node_id", nullable = false)
    private Long examScopeNodeId;

    @Column(name = "learning_content_id")
    private Long learningContentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LearningProgressStatus status = LearningProgressStatus.NOT_STARTED;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_studied_at")
    private LocalDateTime lastStudiedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public LearningProgress(Long userId, Long examScopeNodeId, Long learningContentId) {
        this.userId = userId;
        this.examScopeNodeId = examScopeNodeId;
        this.learningContentId = learningContentId;
        this.status = LearningProgressStatus.IN_PROGRESS;
        this.lastStudiedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = LearningProgressStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.lastStudiedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
        this.lastStudiedAt = LocalDateTime.now();
    }

    public void increaseCorrectCount() {
        this.correctCount++;
    }

    public void increaseWrongCount() {
        this.wrongCount++;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}