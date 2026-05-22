package com.aha.domain.ailearn.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ai_reference")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ai_message_id", nullable = false)
    private Long aiMessageId;

    @Column(name = "learning_content_id")
    private Long learningContentId;

    @Column(name = "learning_content_body_id")
    private Long learningContentBodyId;

    @Column(name = "extracted_content_id")
    private Long extractedContentId;

    @Column(name = "similarity_score")
    private Double similarityScore;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AiReference(Long aiMessageId, Long learningContentId, Long learningContentBodyId,
                       Long extractedContentId, Double similarityScore, Integer displayOrder) {
        this.aiMessageId = aiMessageId;
        this.learningContentId = learningContentId;
        this.learningContentBodyId = learningContentBodyId;
        this.extractedContentId = extractedContentId;
        this.similarityScore = similarityScore;
        this.displayOrder = displayOrder;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}