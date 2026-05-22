package com.aha.domain.ailearn.document.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "extracted_content")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private Boolean isUsedForLearning = false;

    @Column(name = "is_used_for_rag", nullable = false)
    private Boolean isUsedForRag = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ExtractedContent(Long sourceDocumentId, Long processingId, Long examScopeNodeId,
                            Integer chunkOrder, Integer pageNo, String contentText) {
        this.sourceDocumentId = sourceDocumentId;
        this.processingId = processingId;
        this.examScopeNodeId = examScopeNodeId;
        this.chunkOrder = chunkOrder;
        this.pageNo = pageNo;
        this.contentText = contentText;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}