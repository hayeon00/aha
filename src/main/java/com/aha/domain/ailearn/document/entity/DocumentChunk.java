package com.aha.domain.ailearn.document.entity;

import com.aha.domain.ailearn.document.enums.DocumentChunkContentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
        },
        indexes = {
                @Index(
                        name = "idx_document_chunk_source_document",
                        columnList = "source_document_id"
                ),
                @Index(
                        name = "idx_document_chunk_processing",
                        columnList = "processing_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 청크가 만들어진 원본 문서.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_document_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_document_chunk_source_document"
            )
    )
    private SourceDocument sourceDocument;

    /**
     * 청크가 생성된 문서 처리 작업.
     *
     * 처리 이력과의 연결이 필요하지 않은 기존 데이터 등을
     * 고려하여 nullable로 둔다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "processing_id",
            foreignKey = @ForeignKey(
                    name = "fk_document_chunk_processing"
            )
    )
    private DocumentProcessing processing;

    /**
     * 원본 문서 안에서 청크가 등장하는 순서.
     *
     * 첫 번째 청크부터 1, 2, 3... 순서로 저장한다.
     */
    @Column(
            name = "chunk_order",
            nullable = false
    )
    private Integer chunkOrder;

    /**
     * PDF의 페이지 번호.
     *
     * TXT 또는 페이지 정보를 추출하지 못한 문서는 null이다.
     */
    @Column(name = "page_no")
    private Integer pageNo;

    /**
     * DOCX 제목, PDF 섹션 제목 등 청크가 속한 구역의 제목.
     */
    @Column(
            name = "section_title",
            length = 255
    )
    private String sectionTitle;

    /**
     * 청크 내용 유형.
     *
     * TEXT, TITLE, LIST, TABLE, CODE,
     * IMAGE_DESCRIPTION, UNKNOWN
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "content_type",
            nullable = false,
            length = 30
    )
    private DocumentChunkContentType contentType;

    /**
     * 목차 매핑과 개념설명 생성에 사용할 정제된 청크 본문.
     */
    @Lob
    @Column(
            name = "content_text",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String contentText;

    /**
     * 정제하기 전의 원본 청크 텍스트.
     */
    @Lob
    @Column(
            name = "raw_text",
            columnDefinition = "LONGTEXT"
    )
    private String rawText;

    /**
     * 현재는 청크 요약을 사용하지 않으므로 null로 저장한다.
     * 향후 필요할 경우 사용할 수 있도록 컬럼은 유지한다.
     */
    @Lob
    @Column(
            name = "summary",
            columnDefinition = "TEXT"
    )
    private String summary;

    /**
     * 현재는 키워드 추출을 사용하지 않으므로 null로 저장한다.
     *
     * MySQL JSON 컬럼을 문자열 형태로 매핑한다.
     */
    @Column(
            name = "keywords_json",
            columnDefinition = "JSON"
    )
    private String keywordsJson;

    /**
     * 제목, 표, 목록 등의 구조화 정보를 JSON으로 저장한다.
     * 현재 기본 청킹 단계에서는 null로 저장한다.
     */
    @Column(
            name = "structure_json",
            columnDefinition = "JSON"
    )
    private String structureJson;

    /**
     * 해당 청크의 토큰 수.
     *
     * 토큰 계산을 아직 하지 않는 경우 null로 저장한다.
     */
    @Column(name = "token_count")
    private Integer tokenCount;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Builder
    private DocumentChunk(
            SourceDocument sourceDocument,
            DocumentProcessing processing,
            Integer chunkOrder,
            Integer pageNo,
            String sectionTitle,
            DocumentChunkContentType contentType,
            String contentText,
            String rawText,
            String summary,
            String keywordsJson,
            String structureJson,
            Integer tokenCount
    ) {
        validateSourceDocument(sourceDocument);
        validateChunkOrder(chunkOrder);
        validateContentText(contentText);

        this.sourceDocument = sourceDocument;
        this.processing = processing;
        this.chunkOrder = chunkOrder;
        this.pageNo = pageNo;
        this.sectionTitle = normalizeNullableText(sectionTitle);
        this.contentType = contentType != null
                ? contentType
                : DocumentChunkContentType.TEXT;
        this.contentText = contentText.trim();
        this.rawText = normalizeNullableText(rawText);
        this.summary = normalizeNullableText(summary);
        this.keywordsJson = normalizeNullableText(keywordsJson);
        this.structureJson = normalizeNullableText(structureJson);
        this.tokenCount = tokenCount;
    }

    /**
     * 청크 본문을 다시 정제해야 할 때 사용한다.
     */
    public void updateContentText(String contentText) {
        validateContentText(contentText);
        this.contentText = contentText.trim();
    }

    /**
     * 문서 구조 정보를 나중에 추가할 때 사용한다.
     */
    public void updateStructure(
            Integer pageNo,
            String sectionTitle,
            DocumentChunkContentType contentType,
            String structureJson
    ) {
        this.pageNo = pageNo;
        this.sectionTitle =
                normalizeNullableText(sectionTitle);
        this.contentType = contentType != null
                ? contentType
                : DocumentChunkContentType.TEXT;
        this.structureJson =
                normalizeNullableText(structureJson);
    }

    /**
     * 토큰 수를 계산한 후 저장할 때 사용한다.
     */
    public void updateTokenCount(Integer tokenCount) {
        if (tokenCount != null && tokenCount < 0) {
            throw new IllegalArgumentException(
                    "토큰 수는 0 이상이어야 합니다."
            );
        }

        this.tokenCount = tokenCount;
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

    private void validateChunkOrder(
            Integer chunkOrder
    ) {
        if (chunkOrder == null || chunkOrder < 1) {
            throw new IllegalArgumentException(
                    "청크 순서는 1 이상이어야 합니다."
            );
        }
    }

    private void validateContentText(
            String contentText
    ) {
        if (contentText == null
                || contentText.isBlank()) {

            throw new IllegalArgumentException(
                    "청크 본문은 필수입니다."
            );
        }
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}