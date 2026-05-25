package com.aha.domain.ailearn.session.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "learning_session",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_learning_session_user_node",
                        columnNames = {"user_id", "exam_scope_node_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningSession {

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exam_scope_node_id", nullable = false)
    private Long examScopeNodeId;

    @Column(name = "learning_content_id")
    private Long learningContentId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LearningSession(Long userId, Long examScopeNodeId, Long learningContentId) {
        this.userId = userId;
        this.examScopeNodeId = examScopeNodeId;
        this.learningContentId = learningContentId;
        this.status = STATUS_IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }

    public void touch() {
        this.lastAccessedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = STATUS_COMPLETED;
        this.endedAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(this.status);
    }

    public boolean isInProgress() {
        return STATUS_IN_PROGRESS.equals(this.status);
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (this.status == null) {
            this.status = STATUS_IN_PROGRESS;
        }

        if (this.startedAt == null) {
            this.startedAt = now;
        }

        if (this.lastAccessedAt == null) {
            this.lastAccessedAt = now;
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