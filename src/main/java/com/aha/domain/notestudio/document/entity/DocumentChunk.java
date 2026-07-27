package com.aha.domain.notestudio.document.entity;

import com.aha.domain.notestudio.document.enums.DocumentChunkCodeLanguage;
import com.aha.domain.notestudio.document.enums.DocumentChunkContentType;
import com.aha.domain.notestudio.document.enums.DocumentChunkMappingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "document_chunk",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_chunk_source_order",
                        columnNames = {
                                "source_document_id",
                                "chunk_order"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_document_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_document_chunk_source_document"
            )
    )
    private SourceDocument sourceDocument;

    @Column(name = "chunk_order", nullable = false)
    private int chunkOrder;

    @Column(name = "page_no")
    private Integer pageNo;

    @Column(name = "section_title", length = 255)
    private String sectionTitle;

    @Column(name = "heading_path", length = 1000)
    private String headingPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 30)
    private DocumentChunkContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "code_language", length = 30)
    private DocumentChunkCodeLanguage codeLanguage;

    @Lob
    @Column(name = "content_text", nullable = false, columnDefinition = "LONGTEXT")
    private String contentText;

    @Lob
    @Column(name = "raw_text", columnDefinition = "LONGTEXT")
    private String rawText;

    @Lob
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "keywords_json", columnDefinition = "JSON")
    private String keywordsJson;

    @Column(name = "structure_json", columnDefinition = "JSON")
    private String structureJson;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, length = 30)
    private DocumentChunkMappingStatus mappingStatus;

    @Column(name = "mapping_confidence", precision = 5, scale = 4)
    private BigDecimal mappingConfidence;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private DocumentChunk(
            SourceDocument sourceDocument,
            Integer chunkOrder,
            Integer pageNo,
            String sectionTitle,
            String headingPath,
            DocumentChunkContentType contentType,
            DocumentChunkCodeLanguage codeLanguage,
            String contentText,
            String rawText,
            String summary,
            String keywordsJson,
            String structureJson,
            Integer tokenCount
    ) {
        validateSourceDocument(sourceDocument);
        validateChunkOrder(chunkOrder);
        validatePageNo(pageNo);
        validateContentText(contentText);
        validateTokenCount(tokenCount);

        DocumentChunkContentType resolvedContentType = resolveContentType(contentType);

        this.sourceDocument = sourceDocument;
        this.chunkOrder = chunkOrder;
        this.pageNo = pageNo;
        this.sectionTitle = normalizeNullableText(sectionTitle);
        this.headingPath = normalizeNullableText(headingPath);
        this.contentType = resolvedContentType;
        this.codeLanguage = resolveCodeLanguage(resolvedContentType, codeLanguage);
        this.contentText = contentText.trim();
        this.rawText = normalizeNullableText(rawText);
        this.summary = normalizeNullableText(summary);
        this.keywordsJson = normalizeNullableText(keywordsJson);
        this.structureJson = normalizeNullableText(structureJson);
        this.tokenCount = tokenCount;
        this.mappingStatus = DocumentChunkMappingStatus.UNASSIGNED;
        this.mappingConfidence = null;
    }

    public void markUnassigned(BigDecimal confidence) {
        this.mappingStatus = DocumentChunkMappingStatus.UNASSIGNED;
        this.mappingConfidence = normalizeConfidence(confidence);
    }

    public void markAutoMapped(BigDecimal confidence) {
        this.mappingStatus = DocumentChunkMappingStatus.AUTO_MAPPED;
        this.mappingConfidence = normalizeConfidence(confidence);
    }

    public void markManualMapped() {
        this.mappingStatus = DocumentChunkMappingStatus.MANUAL_MAPPED;
        this.mappingConfidence = BigDecimal.ONE.setScale(4);
    }

    private BigDecimal normalizeConfidence(BigDecimal confidence) {
        if (confidence == null) {
            return null;
        }
        if (confidence.compareTo(BigDecimal.ZERO) < 0 || confidence.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("매핑 신뢰도는 0 이상 1 이하여야 합니다.");
        }
        return confidence.setScale(4, java.math.RoundingMode.HALF_UP);
    }

    public void updateContentText(String contentText) {
        validateContentText(contentText);
        this.contentText = contentText.trim();
    }

    public void updateStructure(
            Integer pageNo,
            String sectionTitle,
            String headingPath,
            DocumentChunkContentType contentType,
            DocumentChunkCodeLanguage codeLanguage,
            String structureJson
    ) {
        validatePageNo(pageNo);

        DocumentChunkContentType resolvedContentType = resolveContentType(contentType);

        this.pageNo = pageNo;
        this.sectionTitle = normalizeNullableText(sectionTitle);
        this.headingPath = normalizeNullableText(headingPath);
        this.contentType = resolvedContentType;
        this.codeLanguage = resolveCodeLanguage(resolvedContentType, codeLanguage);
        this.structureJson = normalizeNullableText(structureJson);
    }

    public void updateTokenCount(Integer tokenCount) {
        validateTokenCount(tokenCount);
        this.tokenCount = tokenCount;
    }

    private DocumentChunkContentType resolveContentType(
            DocumentChunkContentType contentType
    ) {
        if (contentType == null) {
            return DocumentChunkContentType.TEXT;
        }

        return contentType;
    }

    private DocumentChunkCodeLanguage resolveCodeLanguage(
            DocumentChunkContentType contentType,
            DocumentChunkCodeLanguage codeLanguage
    ) {
        if (!supportsCodeLanguage(contentType)) {
            return null;
        }

        if (codeLanguage == null) {
            return DocumentChunkCodeLanguage.UNKNOWN;
        }

        return codeLanguage;
    }

    private boolean supportsCodeLanguage(DocumentChunkContentType contentType) {
        return contentType == DocumentChunkContentType.CODE
                || contentType == DocumentChunkContentType.COMMAND
                || contentType == DocumentChunkContentType.CONFIG;
    }

    private void validateSourceDocument(
            SourceDocument sourceDocument
    ) {
        if (sourceDocument == null) {
            throw new IllegalArgumentException(
                    "원본 문서는 필수입니다."
            );
        }
    }

    private void validateChunkOrder(Integer chunkOrder) {
        if (chunkOrder == null || chunkOrder < 1) {
            throw new IllegalArgumentException(
                    "청크 순서는 1 이상이어야 합니다."
            );
        }
    }

    private void validatePageNo(Integer pageNo) {
        if (pageNo != null && pageNo < 1) {
            throw new IllegalArgumentException(
                    "페이지 번호는 1 이상이어야 합니다."
            );
        }
    }

    private void validateContentText(String contentText) {
        if (contentText == null || contentText.isBlank()) {
            throw new IllegalArgumentException(
                    "청크 본문은 필수입니다."
            );
        }
    }

    private void validateTokenCount(Integer tokenCount) {
        if (tokenCount != null && tokenCount < 0) {
            throw new IllegalArgumentException(
                    "토큰 수는 0 이상이어야 합니다."
            );
        }
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
