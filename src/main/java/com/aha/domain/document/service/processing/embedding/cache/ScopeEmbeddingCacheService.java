package com.aha.domain.document.service.processing.embedding.cache;

import com.aha.domain.document.entity.ExamScopeNodeEmbedding;
import com.aha.domain.document.repository.ExamScopeNodeEmbeddingRepository;
import com.aha.domain.document.service.processing.embedding.EmbeddingModelProvider;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ScopeEmbeddingCacheService {

    /*
     * 시험 버전 단위 snapshot을 캐싱한다.
     *
     * maximumSize는 "Topic 수"가 아니라 "시험 버전 + 모델 조합 수" 기준이다.
     * 예: SQLD 2026 / 정보처리기사 2026 / ADsP 2026 ...
     */
    private static final long MAX_CACHE_ENTRIES = 100;

    /*
     * 목차 임베딩은 매우 안정적인 데이터이므로 비교적 길게 캐싱한다.
     * 변경 시 명시적 evict를 수행하므로 TTL은 메모리 회수용 안전장치다.
     */
    private static final Duration CACHE_EXPIRE_AFTER_ACCESS =
            Duration.ofHours(6);

    private final ExamScopeNodeEmbeddingRepository examScopeNodeEmbeddingRepository;
    private final EmbeddingModelProvider embeddingModelProvider;

    private final Cache<ScopeEmbeddingCacheKey, ScopeEmbeddingSnapshot> cache =
            Caffeine.newBuilder()
                    .maximumSize(MAX_CACHE_ENTRIES)
                    .expireAfterAccess(CACHE_EXPIRE_AFTER_ACCESS)
                    .recordStats()
                    .build();

    public ScopeEmbeddingCacheService(
            ExamScopeNodeEmbeddingRepository examScopeNodeEmbeddingRepository,
            EmbeddingModelProvider embeddingModelProvider
    ) {
        this.examScopeNodeEmbeddingRepository = examScopeNodeEmbeddingRepository;
        this.embeddingModelProvider = embeddingModelProvider;
    }

    public ScopeEmbeddingSnapshot get(
            Long examVersionId,
            List<ExamScopeNode> scopeNodes
    ) {
        validateInput(
                examVersionId,
                scopeNodes
        );

        String embeddingProvider =
                embeddingModelProvider.getEmbeddingProvider();

        String embeddingModel =
                embeddingModelProvider.getEmbeddingModel();

        ScopeEmbeddingCacheKey cacheKey =
                new ScopeEmbeddingCacheKey(
                        examVersionId,
                        embeddingProvider,
                        embeddingModel
                );

        ScopeEmbeddingSnapshot cached =
                cache.getIfPresent(cacheKey);

        if (cached != null) {
            log.debug(
                    "시험 목차 임베딩 캐시 HIT. examVersionId={}, provider={}, model={}, scopeCount={}",
                    examVersionId,
                    embeddingProvider,
                    embeddingModel,
                    cached.embeddingByScopeNodeId().size()
            );

            return cached;
        }

        ScopeEmbeddingSnapshot loaded =
                loadSnapshot(
                        examVersionId,
                        scopeNodes,
                        embeddingProvider,
                        embeddingModel
                );

        cache.put(
                cacheKey,
                loaded
        );

        log.info(
                "시험 목차 임베딩 캐시 MISS -> 적재 완료. examVersionId={}, provider={}, model={}, scopeCount={}",
                examVersionId,
                embeddingProvider,
                embeddingModel,
                loaded.embeddingByScopeNodeId().size()
        );

        return loaded;
    }

    /**
     * 해당 시험 버전의 모든 provider/model cache를 제거한다.
     *
     * 시험 목차 내용이나 embedding이 갱신된 직후 호출한다.
     */
    public void evictExamVersion(
            Long examVersionId
    ) {
        if (examVersionId == null) {
            return;
        }

        cache.asMap()
                .keySet()
                .removeIf(key ->
                        Objects.equals(
                                key.examVersionId(),
                                examVersionId
                        )
                );

        log.info(
                "시험 목차 임베딩 캐시 제거. examVersionId={}",
                examVersionId
        );
    }

    public void evictAll() {
        cache.invalidateAll();
    }

    public long estimatedSize() {
        return cache.estimatedSize();
    }

    private ScopeEmbeddingSnapshot loadSnapshot(
            Long examVersionId,
            List<ExamScopeNode> scopeNodes,
            String embeddingProvider,
            String embeddingModel
    ) {
        List<Long> scopeNodeIds =
                scopeNodes.stream()
                        .filter(Objects::nonNull)
                        .map(ExamScopeNode::getId)
                        .filter(Objects::nonNull)
                        .toList();

        if (scopeNodeIds.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        List<ExamScopeNodeEmbedding> embeddings =
                examScopeNodeEmbeddingRepository
                        .findAllByExamScopeNode_IdInAndEmbeddingProviderAndEmbeddingModel(
                                scopeNodeIds,
                                embeddingProvider,
                                embeddingModel
                        );

        Map<Long, List<Double>> embeddingByScopeNodeId =
                new LinkedHashMap<>();

        for (ExamScopeNodeEmbedding embedding : embeddings) {

            if (embedding == null
                    || embedding.getExamScopeNode() == null
                    || embedding.getExamScopeNode().getId() == null
                    || embedding.getEmbeddingJson() == null
                    || embedding.getEmbeddingJson().isEmpty()) {
                continue;
            }

            embeddingByScopeNodeId.put(
                    embedding.getExamScopeNode().getId(),
                    List.copyOf(
                            embedding.getEmbeddingJson()
                    )
            );
        }

        if (embeddingByScopeNodeId.size()
                != scopeNodeIds.size()) {

            log.warn(
                    "일부 시험 목차 임베딩이 누락되었습니다. examVersionId={}, requestedCount={}, loadedCount={}",
                    examVersionId,
                    scopeNodeIds.size(),
                    embeddingByScopeNodeId.size()
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        return new ScopeEmbeddingSnapshot(
                examVersionId,
                embeddingProvider,
                embeddingModel,
                embeddingByScopeNodeId
        );
    }

    private void validateInput(
            Long examVersionId,
            List<ExamScopeNode> scopeNodes
    ) {
        if (examVersionId == null
                || examVersionId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (scopeNodes == null
                || scopeNodes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }
    }
}