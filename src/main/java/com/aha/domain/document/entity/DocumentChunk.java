package com.aha.domain.document.entity;

import com.aha.domain.document.enums.DocumentChunkContentType;
import com.aha.domain.document.enums.DocumentChunkMappingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Getter
@Entity
@Table(
        name = "document_chunk",
        indexes = {
                @Index(
                        name = "idx_chunk_source_page",
                        columnList = "source_document_id, page_start, page_end"
                ),
                @Index(
                        name = "idx_chunk_content_type",
                        columnList = "content_type"
                ),
                @Index(
                        name = "idx_chunk_mapping_status",
                        columnList = "mapping_status"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chunk_source_order",
                        columnNames = {
                                "source_document_id",
                                "chunk_order"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DocumentChunk {

    private static final int MAX_SECTION_TITLE_LENGTH = 255;
    private static final int MAX_HEADING_PATH_LENGTH = 1000;
    private static final int MAX_CODE_LANGUAGE_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_document_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_chunk_source_document"
            )
    )
    private SourceDocument sourceDocument;

    @Column(name = "chunk_order", nullable = false)
    private int chunkOrder;

    @Column(name = "page_start")
    private Integer pageStart;

    @Column(name = "page_end")
    private Integer pageEnd;

    @Column(name = "section_title", length = 255)
    private String sectionTitle;

    @Column(name = "heading_path", length = 1000)
    private String headingPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 30)
    private DocumentChunkContentType contentType;

    @Column(name = "code_language", length = 30)
    private String codeLanguage;

    @Lob
    @Column(name = "content_text", nullable = false, columnDefinition = "LONGTEXT")
    private String contentText;

    @Lob
    @Column(name = "raw_text", columnDefinition = "LONGTEXT")
    private String rawText;

    @Lob
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keywords_json", columnDefinition = "JSON")
    private Map<String, Object> keywordsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structure_json", columnDefinition = "JSON")
    private Map<String, Object> structureJson;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, length = 30)
    private DocumentChunkMappingStatus mappingStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static DocumentChunk create(
            SourceDocument sourceDocument,
            int chunkOrder,
            Integer pageStart,
            Integer pageEnd,
            String sectionTitle,
            String headingPath,
            DocumentChunkContentType contentType,
            String codeLanguage,
            String contentText,
            String rawText,
            String summary,
            Map<String, Object> keywordsJson,
            Map<String, Object> structureJson,
            Integer tokenCount
    ) {
        validateSourceDocument(sourceDocument);
        validateChunkOrder(chunkOrder);
        validatePages(pageStart, pageEnd);
        validateContentText(contentText);
        validateTokenCount(tokenCount);

        DocumentChunkContentType resolvedContentType =
                resolveContentType(contentType);

        return DocumentChunk.builder()
                .sourceDocument(sourceDocument)
                .chunkOrder(chunkOrder)
                .pageStart(pageStart)
                .pageEnd(pageEnd)
                .sectionTitle(
                        normalizeNullableText(
                                sectionTitle,
                                MAX_SECTION_TITLE_LENGTH
                        )
                )
                .headingPath(
                        normalizeNullableText(
                                headingPath,
                                MAX_HEADING_PATH_LENGTH
                        )
                )
                .contentType(resolvedContentType)
                .codeLanguage(
                        resolveCodeLanguage(
                                resolvedContentType,
                                codeLanguage
                        )
                )
                .contentText(contentText.trim())
                .rawText(
                        normalizeNullableText(rawText)
                )
                .summary(
                        normalizeNullableText(summary)
                )
                .keywordsJson(keywordsJson)
                .structureJson(structureJson)
                .tokenCount(tokenCount)
                .mappingStatus(
                        DocumentChunkMappingStatus.UNASSIGNED
                )
                .build();
    }

    public void markMapped() {
        this.mappingStatus =
                DocumentChunkMappingStatus.MAPPED;
    }

    public void markAmbiguous() {
        this.mappingStatus =
                DocumentChunkMappingStatus.AMBIGUOUS;
    }

    public void markRejected() {
        this.mappingStatus =
                DocumentChunkMappingStatus.REJECTED;
    }

    public void markUnassigned() {
        this.mappingStatus =
                DocumentChunkMappingStatus.UNASSIGNED;
    }

    public void updateSummary(
            String summary
    ) {
        this.summary =
                normalizeNullableText(summary);
    }

    public void updateKeywords(
            Map<String, Object> keywordsJson
    ) {
        this.keywordsJson = keywordsJson;
    }

    public void updateTokenCount(
            Integer tokenCount
    ) {
        validateTokenCount(tokenCount);
        this.tokenCount = tokenCount;
    }

    private static DocumentChunkContentType resolveContentType(
            DocumentChunkContentType contentType
    ) {
        return contentType == null
                ? DocumentChunkContentType.TEXT
                : contentType;
    }

    private static String resolveCodeLanguage(
            DocumentChunkContentType contentType,
            String codeLanguage
    ) {
        if (!supportsCodeLanguage(contentType)) {
            return null;
        }

        if (codeLanguage == null
                || codeLanguage.isBlank()) {
            return null;
        }

        String normalized =
                codeLanguage.trim()
                        .toUpperCase(Locale.ROOT);

        if (normalized.length()
                > MAX_CODE_LANGUAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "코드 언어는 30자를 초과할 수 없습니다."
            );
        }

        return normalized;
    }

    private static boolean supportsCodeLanguage(
            DocumentChunkContentType contentType
    ) {
        return contentType
                == DocumentChunkContentType.CODE
                || contentType
                == DocumentChunkContentType.COMMAND
                || contentType
                == DocumentChunkContentType.CONFIG;
    }

    private static void validateSourceDocument(
            SourceDocument sourceDocument
    ) {
        if (sourceDocument == null) {
            throw new IllegalArgumentException(
                    "원본 문서는 필수입니다."
            );
        }
    }

    private static void validateChunkOrder(
            int chunkOrder
    ) {
        if (chunkOrder < 1) {
            throw new IllegalArgumentException(
                    "청크 순서는 1 이상이어야 합니다."
            );
        }
    }

    private static void validatePages(
            Integer pageStart,
            Integer pageEnd
    ) {
        if (pageStart != null
                && pageStart < 1) {
            throw new IllegalArgumentException(
                    "시작 페이지는 1 이상이어야 합니다."
            );
        }

        if (pageEnd != null
                && pageEnd < 1) {
            throw new IllegalArgumentException(
                    "종료 페이지는 1 이상이어야 합니다."
            );
        }

        if (pageStart != null
                && pageEnd != null
                && pageEnd < pageStart) {
            throw new IllegalArgumentException(
                    "종료 페이지는 시작 페이지보다 작을 수 없습니다."
            );
        }
    }

    private static void validateContentText(
            String contentText
    ) {
        if (contentText == null
                || contentText.isBlank()) {
            throw new IllegalArgumentException(
                    "청크 본문은 필수입니다."
            );
        }
    }

    private static void validateTokenCount(
            Integer tokenCount
    ) {
        if (tokenCount != null
                && tokenCount < 0) {
            throw new IllegalArgumentException(
                    "토큰 수는 0 이상이어야 합니다."
            );
        }
    }

    private static String normalizeNullableText(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private static String normalizeNullableText(
            String value,
            int maxLength
    ) {
        String normalized =
                normalizeNullableText(value);

        if (normalized == null) {
            return null;
        }

        if (normalized.length()
                > maxLength) {
            throw new IllegalArgumentException(
                    "문자열 길이는 "
                            + maxLength
                            + "자를 초과할 수 없습니다."
            );
        }

        return normalized;
    }
}