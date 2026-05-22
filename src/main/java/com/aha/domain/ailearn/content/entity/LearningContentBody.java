package com.aha.domain.ailearn.content.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "learning_content_body")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningContentBody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "learning_content_id", nullable = false)
    private Long learningContentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type", nullable = false, length = 50)
    private ContentBodyType bodyType;

    @Column(name = "title", length = 200)
    private String title;

    @Lob
    @Column(name = "body_text", nullable = false)
    private String bodyText;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "rag_chunk_order")
    private Integer ragChunkOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public LearningContentBody(Long learningContentId, ContentBodyType bodyType, String title,
                               String bodyText, Integer displayOrder, Integer ragChunkOrder) {
        this.learningContentId = learningContentId;
        this.bodyType = bodyType;
        this.title = title;
        this.bodyText = bodyText;
        this.displayOrder = displayOrder;
        this.ragChunkOrder = ragChunkOrder;
        this.isActive = true;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}