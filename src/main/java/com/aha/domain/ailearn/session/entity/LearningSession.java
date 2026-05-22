package com.aha.domain.ailearn.session.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "learning_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exam_scope_node_id", nullable = false)
    private Long examScopeNodeId;

    @Column(name = "learning_content_id")
    private Long learningContentId;

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
        this.startedAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }

    public void touch() {
        this.lastAccessedAt = LocalDateTime.now();
    }

    public void end() {
        this.endedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}