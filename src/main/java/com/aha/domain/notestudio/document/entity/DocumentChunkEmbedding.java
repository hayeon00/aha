package com.aha.domain.notestudio.document.entity;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
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
        name = "document_chunk_embedding",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_chunk_embedding_chunk_model",
                        columnNames = {"document_chunk_id", "embedding_model"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_document_chunk_embedding_chunk",
                        columnList = "document_chunk_id"
                ),
                @Index(
                        name = "idx_document_chunk_embedding_model",
                        columnList = "embedding_model"
                ),
                @Index(
                        name = "idx_document_chunk_embedding_text_hash",
                        columnList = "embedding_text_hash"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentChunkEmbedding {

    private static final int MAX_EMBEDDING_MODEL_LENGTH = 100;
    private static final int MAX_EMBEDDING_TEXT_HASH_LENGTH = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_chunk_id", nullable = false)
    private DocumentChunk documentChunk;

    @Column(name = "embedding_model", nullable = false, length = MAX_EMBEDDING_MODEL_LENGTH)
    private String embeddingModel;

    @Column(name = "embedding_json", nullable = false, columnDefinition = "json")
    private String embeddingJson;

    @Column(name = "embedding_dimension", nullable = false)
    private Integer embeddingDimension;

    @Column(name = "embedding_text_hash", nullable = false, length = MAX_EMBEDDING_TEXT_HASH_LENGTH)
    private String embeddingTextHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private DocumentChunkEmbedding(
            DocumentChunk documentChunk,
            String embeddingModel,
            String embeddingJson,
            Integer embeddingDimension,
            String embeddingTextHash
    ) {
        validateDocumentChunk(documentChunk);
        validateEmbeddingModel(embeddingModel);
        validateEmbeddingJson(embeddingJson);
        validateEmbeddingDimension(embeddingDimension);
        validateEmbeddingTextHash(embeddingTextHash);

        this.documentChunk = documentChunk;
        this.embeddingModel = embeddingModel.trim();
        this.embeddingJson = embeddingJson.trim();
        this.embeddingDimension = embeddingDimension;
        this.embeddingTextHash = embeddingTextHash.trim();
    }

    public void updateEmbedding(
            String embeddingJson,
            Integer embeddingDimension,
            String embeddingTextHash
    ) {
        validateEmbeddingJson(embeddingJson);
        validateEmbeddingDimension(embeddingDimension);
        validateEmbeddingTextHash(embeddingTextHash);

        this.embeddingJson = embeddingJson.trim();
        this.embeddingDimension = embeddingDimension;
        this.embeddingTextHash = embeddingTextHash.trim();
    }

    public boolean hasSameTextHash(String embeddingTextHash) {
        if (embeddingTextHash == null || embeddingTextHash.isBlank()) {
            return false;
        }

        return this.embeddingTextHash.equals(embeddingTextHash.trim());
    }

    private void validateDocumentChunk(DocumentChunk documentChunk) {
        if (documentChunk == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateEmbeddingModel(String embeddingModel) {
        if (embeddingModel == null || embeddingModel.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (embeddingModel.length() > MAX_EMBEDDING_MODEL_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateEmbeddingJson(String embeddingJson) {
        if (embeddingJson == null || embeddingJson.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String normalizedEmbeddingJson = embeddingJson.trim();

        if (!normalizedEmbeddingJson.startsWith("[") || !normalizedEmbeddingJson.endsWith("]")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateEmbeddingDimension(Integer embeddingDimension) {
        if (embeddingDimension == null || embeddingDimension <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateEmbeddingTextHash(String embeddingTextHash) {
        if (embeddingTextHash == null || embeddingTextHash.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (embeddingTextHash.length() > MAX_EMBEDDING_TEXT_HASH_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
