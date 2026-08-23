package com.aha.domain.document.service.processing.embedding.cache;

/**
 * 시험 목차 임베딩 캐시 키.
 *
 * 동일 시험 버전이라도 embedding provider/model이 변경되면
 * 서로 다른 embedding space이므로 반드시 별도 캐시로 관리한다.
 */
public record ScopeEmbeddingCacheKey(
        Long examVersionId,
        String embeddingProvider,
        String embeddingModel
) {
}