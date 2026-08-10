package com.aha.domain.document.entity;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Table(
        name = "document_chunk_embedding",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chunk_embedding_model",
                        columnNames = {
                                "document_chunk_id",
                                "embedding_provider",
                                "embedding_model"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_chunk_embedding_model",
                        columnList = "embedding_provider, embedding_model"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DocumentChunkEmbedding {

    private static final int MAX_EMBEDDING_PROVIDER_LENGTH = 50;
    private static final int MAX_EMBEDDING_MODEL_LENGTH = 100;
    private static final int MAX_EMBEDDING_TEXT_HASH_LENGTH = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_chunk_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_chunk_embedding_chunk"
            )
    )
    private DocumentChunk documentChunk;

    @Column(name = "embedding_provider", nullable = false, length = 50)
    private String embeddingProvider;

    @Column(name = "embedding_model", nullable = false, length = 100)
    private String embeddingModel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "embedding_json", nullable = false, columnDefinition = "JSON")
    private List<Double> embeddingJson;

    @Column(name = "embedding_dimension", nullable = false)
    private int embeddingDimension;

    @Column(name = "embedding_text_hash", nullable = false, length = 64)
    private String embeddingTextHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static DocumentChunkEmbedding create(
            DocumentChunk documentChunk,
            String embeddingProvider,
            String embeddingModel,
            List<Double> embeddingJson,
            String embeddingTextHash
    ) {
        validateDocumentChunk(documentChunk);
        validateEmbeddingProvider(embeddingProvider);
        validateEmbeddingModel(embeddingModel);
        validateEmbeddingJson(embeddingJson);
        validateEmbeddingTextHash(embeddingTextHash);

        return DocumentChunkEmbedding.builder()
                .documentChunk(documentChunk)
                .embeddingProvider(
                        embeddingProvider.trim()
                )
                .embeddingModel(
                        embeddingModel.trim()
                )
                .embeddingJson(
                        List.copyOf(embeddingJson)
                )
                .embeddingDimension(
                        embeddingJson.size()
                )
                .embeddingTextHash(
                        embeddingTextHash.trim()
                )
                .build();
    }

    public void updateEmbedding(
            List<Double> embeddingJson,
            String embeddingTextHash
    ) {
        validateEmbeddingJson(embeddingJson);
        validateEmbeddingTextHash(embeddingTextHash);

        this.embeddingJson =
                List.copyOf(embeddingJson);

        this.embeddingDimension =
                embeddingJson.size();

        this.embeddingTextHash =
                embeddingTextHash.trim();
    }

    public boolean hasSameTextHash(
            String embeddingTextHash
    ) {
        if (embeddingTextHash == null
                || embeddingTextHash.isBlank()) {
            return false;
        }

        return this.embeddingTextHash.equals(
                embeddingTextHash.trim()
        );
    }

    private static void validateDocumentChunk(
            DocumentChunk documentChunk
    ) {
        if (documentChunk == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private static void validateEmbeddingProvider(
            String embeddingProvider
    ) {
        if (embeddingProvider == null
                || embeddingProvider.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (embeddingProvider.trim().length()
                > MAX_EMBEDDING_PROVIDER_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private static void validateEmbeddingModel(
            String embeddingModel
    ) {
        if (embeddingModel == null
                || embeddingModel.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (embeddingModel.trim().length()
                > MAX_EMBEDDING_MODEL_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private static void validateEmbeddingJson(
            List<Double> embeddingJson
    ) {
        if (embeddingJson == null
                || embeddingJson.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (embeddingJson.stream()
                .anyMatch(value -> value == null)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private static void validateEmbeddingDimension(
            int embeddingDimension,
            List<Double> embeddingJson
    ) {
        if (embeddingDimension <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (embeddingJson.size()
                != embeddingDimension) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private static void validateEmbeddingTextHash(
            String embeddingTextHash
    ) {
        if (embeddingTextHash == null
                || embeddingTextHash.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (embeddingTextHash.trim().length()
                > MAX_EMBEDDING_TEXT_HASH_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }
}