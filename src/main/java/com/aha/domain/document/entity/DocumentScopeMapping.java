package com.aha.domain.document.entity;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "document_scope_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dsm_chunk_scope",
                        columnNames = {
                                "document_chunk_id",
                                "exam_scope_node_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_dsm_document_chunk_id",
                        columnList = "document_chunk_id"
                ),
                @Index(
                        name = "idx_dsm_exam_scope_node_id",
                        columnList = "exam_scope_node_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentScopeMapping {

    private static final BigDecimal MIN_CONFIDENCE_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAX_CONFIDENCE_SCORE = BigDecimal.ONE;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_chunk_id", nullable = false)
    private DocumentChunk documentChunk;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_scope_node_id", nullable = false)
    private ExamScopeNode examScopeNode;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "mapping_reason", length = 1000)
    private String mappingReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private DocumentScopeMapping(
            DocumentChunk documentChunk,
            ExamScopeNode examScopeNode,
            BigDecimal confidenceScore,
            Integer rankNo,
            String mappingReason
    ) {
        if (documentChunk == null) {
            throw new IllegalArgumentException(
                    "문서 청크는 필수입니다."
            );
        }

        if (examScopeNode == null) {
            throw new IllegalArgumentException(
                    "시험 목차 노드는 필수입니다."
            );
        }

        validateConfidenceScore(confidenceScore);

        this.documentChunk = documentChunk;
        this.examScopeNode = examScopeNode;
        this.confidenceScore = confidenceScore;
        this.rankNo = rankNo;
        this.mappingReason = normalizeMappingReason(
                mappingReason
        );
    }

    public void updateMappingResult(
            BigDecimal confidenceScore,
            String mappingReason
    ) {
        validateConfidenceScore(confidenceScore);

        this.confidenceScore = confidenceScore;
        this.mappingReason = normalizeMappingReason(
                mappingReason
        );
    }

    private void validateConfidenceScore(
            BigDecimal confidenceScore
    ) {
        if (confidenceScore == null) {
            return;
        }

        if (confidenceScore.compareTo(
                MIN_CONFIDENCE_SCORE
        ) < 0
                || confidenceScore.compareTo(
                MAX_CONFIDENCE_SCORE
        ) > 0) {

            throw new IllegalArgumentException(
                    "목차 매핑 신뢰도는 0 이상 1 이하여야 합니다."
            );
        }
    }

    private String normalizeMappingReason(
            String mappingReason
    ) {
        if (mappingReason == null
                || mappingReason.isBlank()) {
            return null;
        }

        String normalizedReason =
                mappingReason.trim();

        if (normalizedReason.length() > 1000) {
            return normalizedReason.substring(
                    0,
                    1000
            );
        }

        return normalizedReason;
    }

    private void validateRankNo(Integer rankNo) {
        if (rankNo == null || rankNo < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
