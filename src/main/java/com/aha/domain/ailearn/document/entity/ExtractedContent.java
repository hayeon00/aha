package com.aha.domain.ailearn.document.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "extracted_content")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExtractedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_document_id", nullable = false)
    private Long sourceDocumentId;

    @Column(name = "processing_id")
    private Long processingId;

    @Column(name = "exam_scope_node_id")
    private Long examScopeNodeId;

    @Column(name = "chunk_order", nullable = false)
    private Integer chunkOrder;

    @Column(name = "page_no")
    private Integer pageNo;

    @Lob
    @Column(name = "content_text", nullable = false)
    private String contentText;

    @Column(name = "is_used_for_learning", nullable = false)
    private Boolean isUsedForLearning;

    @Column(name = "is_used_for_rag", nullable = false)
    private Boolean isUsedForRag;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (isUsedForLearning == null) {
            isUsedForLearning = false;
        }

        if (isUsedForRag == null) {
            isUsedForRag = true;
        }

        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}