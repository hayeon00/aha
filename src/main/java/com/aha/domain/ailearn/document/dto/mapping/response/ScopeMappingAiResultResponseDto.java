package com.aha.domain.ailearn.document.dto.mapping.response;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentScopeMappingAiResult
 * @since : 2026. 6. 24. 수요일
 */

import java.math.BigDecimal;

public record ScopeMappingAiResultResponseDto(
        Long documentChunkId,
        Long examScopeNodeId,
        BigDecimal confidenceScore,
        String mappingReason
) {
}