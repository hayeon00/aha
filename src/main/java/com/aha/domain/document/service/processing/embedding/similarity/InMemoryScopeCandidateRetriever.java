package com.aha.domain.document.service.processing.embedding.similarity;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.DocumentChunkEmbedding;
import com.aha.domain.document.repository.DocumentChunkEmbeddingRepository;
import com.aha.domain.document.service.processing.embedding.DocumentEmbeddingService;
import com.aha.domain.document.service.processing.embedding.EmbeddingModelProvider;
import com.aha.domain.document.service.processing.embedding.cache.ScopeEmbeddingCacheService;
import com.aha.domain.document.service.processing.embedding.cache.ScopeEmbeddingSnapshot;
import com.aha.domain.document.service.processing.embedding.similarity.model.ScopeCandidateSearchResult;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryScopeCandidateRetriever
        implements ScopeCandidateRetriever {

    private final EmbeddingModelProvider embeddingModelProvider;
    private final DocumentEmbeddingService documentEmbeddingService;

    private final DocumentChunkEmbeddingRepository documentChunkEmbeddingRepository;

    private final ScopeEmbeddingCacheService scopeEmbeddingCacheService;
    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    @Override
    public Map<Long, List<ScopeCandidateSearchResult>> retrieve(
            Long examVersionId,
            List<DocumentChunk> chunks,
            List<ExamScopeNode> scopeNodes,
            int topN
    ) {
        validateInput(
                examVersionId,
                chunks,
                scopeNodes,
                topN
        );

        /*
         * Chunk는 사용자 문서별 데이터이므로 영속 embedding만 보장한다.
         */
        documentEmbeddingService.ensureChunkEmbeddings(
                chunks
        );

        /*
         * Scope는 시험 버전별 공통 데이터.
         * embedding 생성/갱신 여부를 확인하고,
         * 변경된 경우에만 해당 시험 버전 cache를 무효화한다.
         */
        boolean scopeEmbeddingsChanged =
                documentEmbeddingService.ensureScopeNodeEmbeddings(
                        scopeNodes
                );

        if (scopeEmbeddingsChanged) {
            scopeEmbeddingCacheService.evictExamVersion(
                    examVersionId
            );
        }

        String embeddingProvider =
                embeddingModelProvider.getEmbeddingProvider();

        String embeddingModel =
                embeddingModelProvider.getEmbeddingModel();

        Map<Long, DocumentChunkEmbedding> chunkEmbeddingMap =
                loadChunkEmbeddingMap(
                        chunks,
                        embeddingProvider,
                        embeddingModel
                );

        ScopeEmbeddingSnapshot scopeSnapshot =
                scopeEmbeddingCacheService.get(
                        examVersionId,
                        scopeNodes
                );

        Map<Long, ExamScopeNode> scopeNodeMap =
                scopeNodes.stream()
                        .filter(Objects::nonNull)
                        .filter(node ->
                                node.getId() != null
                        )
                        .collect(
                                Collectors.toMap(
                                        ExamScopeNode::getId,
                                        Function.identity()
                                )
                        );

        Map<Long, List<ScopeCandidateSearchResult>> results =
                new LinkedHashMap<>();

        for (DocumentChunk chunk : chunks) {

            if (chunk == null
                    || chunk.getId() == null) {
                continue;
            }

            DocumentChunkEmbedding chunkEmbedding =
                    chunkEmbeddingMap.get(
                            chunk.getId()
                    );

            if (chunkEmbedding == null
                    || chunkEmbedding.getEmbeddingJson() == null
                    || chunkEmbedding.getEmbeddingJson().isEmpty()) {

                log.warn(
                        "문서 청크 임베딩이 없어 후보 검색에서 제외합니다. documentChunkId={}",
                        chunk.getId()
                );

                continue;
            }

            List<ScopeCandidateSearchResult> topCandidates =
                    findTopCandidates(
                            chunk,
                            chunkEmbedding.getEmbeddingJson(),
                            scopeNodeMap,
                            scopeSnapshot.embeddingByScopeNodeId(),
                            topN
                    );

            results.put(
                    chunk.getId(),
                    topCandidates
            );
        }

        log.info(
                "In-Memory 시험 목차 후보 검색 완료. examVersionId={}, chunkCount={}, scopeNodeCount={}, resultChunkCount={}, topN={}, provider={}, model={}",
                examVersionId,
                chunks.size(),
                scopeNodes.size(),
                results.size(),
                topN,
                embeddingProvider,
                embeddingModel
        );

        return Map.copyOf(results);
    }

    private List<ScopeCandidateSearchResult> findTopCandidates(
            DocumentChunk chunk,
            List<Double> chunkVector,
            Map<Long, ExamScopeNode> scopeNodeMap,
            Map<Long, List<Double>> scopeEmbeddingMap,
            int topN
    ) {
        List<CandidateScore> scores =
                new ArrayList<>(
                        scopeEmbeddingMap.size()
                );

        for (Map.Entry<Long, List<Double>> entry
                : scopeEmbeddingMap.entrySet()) {

            ExamScopeNode scopeNode =
                    scopeNodeMap.get(
                            entry.getKey()
                    );

            if (scopeNode == null) {
                continue;
            }

            List<Double> scopeVector =
                    entry.getValue();

            if (scopeVector == null
                    || scopeVector.isEmpty()) {
                continue;
            }

            double similarity =
                    cosineSimilarityCalculator.calculate(
                            chunkVector,
                            scopeVector
                    );

            scores.add(
                    new CandidateScore(
                            scopeNode,
                            similarity
                    )
            );
        }

        List<CandidateScore> topScores =
                scores.stream()
                        .sorted(
                                Comparator.comparingDouble(
                                        CandidateScore::similarityScore
                                ).reversed()
                        )
                        .limit(topN)
                        .toList();

        // =========================================================
        // 디버깅용 Top-K 로그
        // =========================================================
        log.info(
                "[SCOPE CANDIDATE] chunkId={}, sectionTitle={}, contentPreview={}",
                chunk.getId(),
                chunk.getSectionTitle(),
                preview(chunk.getContentText())
        );

        for (int index = 0;
             index < topScores.size();
             index++) {

            CandidateScore candidate =
                    topScores.get(index);

            log.info(
                    "[SCOPE CANDIDATE] chunkId={}, rank={}, scopeNodeId={}, scopeTitle={}, similarity={}",
                    chunk.getId(),
                    index + 1,
                    candidate.scopeNode().getId(),
                    candidate.scopeNode().getTitle(),
                    String.format("%.4f", candidate.similarityScore())
            );
        }

        List<ScopeCandidateSearchResult> results =
                new ArrayList<>(
                        topScores.size()
                );

        for (int index = 0;
             index < topScores.size();
             index++) {

            CandidateScore candidate =
                    topScores.get(index);

            results.add(
                    new ScopeCandidateSearchResult(
                            chunk.getId(),
                            candidate.scopeNode(),
                            candidate.similarityScore(),
                            index + 1
                    )
            );
        }

        return List.copyOf(results);
    }

    private String preview(
            String text
    ) {
        if (text == null
                || text.isBlank()) {
            return "";
        }

        String normalized =
                text.replace("\n", " ")
                        .trim();

        return normalized.substring(
                0,
                Math.min(
                        normalized.length(),
                        150
                )
        );
    }

    private Map<Long, DocumentChunkEmbedding> loadChunkEmbeddingMap(
            List<DocumentChunk> chunks,
            String embeddingProvider,
            String embeddingModel
    ) {
        List<Long> chunkIds =
                chunks.stream()
                        .filter(Objects::nonNull)
                        .map(DocumentChunk::getId)
                        .filter(Objects::nonNull)
                        .toList();

        if (chunkIds.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }

        Map<Long, DocumentChunkEmbedding> embeddingMap =
                documentChunkEmbeddingRepository
                        .findAllByDocumentChunk_IdInAndEmbeddingProviderAndEmbeddingModel(
                                chunkIds,
                                embeddingProvider,
                                embeddingModel
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        embedding ->
                                                embedding
                                                        .getDocumentChunk()
                                                        .getId(),
                                        Function.identity()
                                )
                        );

        if (embeddingMap.size()
                != chunkIds.size()) {

            log.warn(
                    "일부 문서 청크 임베딩이 누락되었습니다. requestedCount={}, loadedCount={}",
                    chunkIds.size(),
                    embeddingMap.size()
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }

        return embeddingMap;
    }

    private void validateInput(
            Long examVersionId,
            List<DocumentChunk> chunks,
            List<ExamScopeNode> scopeNodes,
            int topN
    ) {
        if (examVersionId == null
                || examVersionId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (chunks == null
                || chunks.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }

        if (scopeNodes == null
                || scopeNodes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        if (topN <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private record CandidateScore(
            ExamScopeNode scopeNode,
            double similarityScore
    ) {
    }
}