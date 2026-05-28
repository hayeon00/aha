package com.aha.domain.ailearn.content.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "learning_content")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_scope_node_id", nullable = false)
    private Long examScopeNodeId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "rag_enabled", nullable = false)
    private Boolean ragEnabled = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public LearningContent(
            Long examScopeNodeId,
            String title,
            String summary,
            Boolean ragEnabled,
            Boolean isActive,
            Integer displayOrder
    ) {
        this.examScopeNodeId = examScopeNodeId;
        this.title = title;
        this.summary = summary;
        this.ragEnabled = ragEnabled != null ? ragEnabled : true;
        this.isActive = isActive != null ? isActive : true;
        this.displayOrder = displayOrder != null ? displayOrder : 1;
    }

    public void deactivate() {
        this.isActive = false;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.ragEnabled == null) {
            this.ragEnabled = true;
        }

        if (this.isActive == null) {
            this.isActive = true;
        }

        if (this.displayOrder == null) {
            this.displayOrder = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}