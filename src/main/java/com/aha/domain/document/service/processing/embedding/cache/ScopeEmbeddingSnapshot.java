package com.aha.domain.document.service.processing.embedding.cache;

import java.util.List;
import java.util.Map;

/**
 * 한 시험 버전에서 사용하는 leaf Topic embedding snapshot.
 *
 * JPA Entity를 장시간 캐시에 보관하지 않고,
 * 후보 검색에 필요한 immutable 값만 보관한다.
 */
public record ScopeEmbeddingSnapshot(
        Long examVersionId,
        String embeddingProvider,
        String embeddingModel,
        Map<Long, List<Double>> embeddingByScopeNodeId
) {

    public ScopeEmbeddingSnapshot {
        embeddingByScopeNodeId = Map.copyOf(embeddingByScopeNodeId);
    }
}