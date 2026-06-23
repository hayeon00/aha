package com.aha.domain.ailearn.document.dto.mapping.response;

import java.math.BigDecimal;

public record DocumentScopeMappingResultDto(
        Long examScopeNodeId,
        String mappingReason
) {
}