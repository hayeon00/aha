package com.aha.domain.ailearn.document.entity;

import com.aha.domain.ailearn.document.type.LearningContentBodyType;
import com.aha.domain.ailearn.document.type.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_generated_learning_content_body")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiGeneratedLearningContentBody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_document_id", nullable = false)
    private Long sourceDocumentId;

    @Column(name = "processing_id", nullable = false)
    private Long processingId;

    @Column(name = "exam_scope_node_id", nullable = false)
    private Long examScopeNodeId;

    @Column(name = "learning_content_id")
    private Long learningContentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type", nullable = false, length = 50)
    private LearningContentBodyType bodyType;

    @Column(length = 100)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 30)
    private ReviewStatus reviewStatus;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (reviewStatus == null) {
            reviewStatus = ReviewStatus.PENDING;
        }

        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void publish() {
        this.reviewStatus = ReviewStatus.PUBLISHED;
    }
}