package com.aha.domain.document.client.mapping.dto;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentScopeMappingAiResult
 * @since : 2026. 6. 24. 수요일
 */

import java.math.BigDecimal;

public record ScopeMappingAiResult(
        Long documentChunkId,
        Long examScopeNodeId,
        BigDecimal confidenceScore,
        String mappingReason
) {
}
