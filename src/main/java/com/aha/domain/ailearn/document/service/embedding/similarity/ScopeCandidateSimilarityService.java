package com.aha.domain.ailearn.document.service.embedding.similarity;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentChunkEmbedding;
import com.aha.domain.ailearn.document.entity.ExamScopeNodeEmbedding;
import com.aha.domain.ailearn.document.repository.DocumentChunkEmbeddingRepository;
import com.aha.domain.ailearn.document.repository.ExamScopeNodeEmbeddingRepository;
import com.aha.domain.ailearn.document.service.embedding.DocumentEmbeddingService;
import com.aha.domain.ailearn.document.service.embedding.EmbeddingModelProvider;
import com.aha.domain.ailearn.document.service.embedding.similarity.dto.ScopeCandidateSearchResultDto;
import com.aha.domain.ailearn.document.service.embedding.util.EmbeddingJsonConverter;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScopeCandidateSimilarityService {

    private final EmbeddingModelProvider embeddingModelProvider;
    private final DocumentEmbeddingService documentEmbeddingService;
    private final DocumentChunkEmbeddingRepository documentChunkEmbeddingRepository;
    private final ExamScopeNodeEmbeddingRepository examScopeNodeEmbeddingRepository;
    private final EmbeddingJsonConverter embeddingJsonConverter;
    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    @Transactional
    public Map<Long, List<ScopeCandidateSearchResultDto>> findTopCandidatesByChunk(
            List<DocumentChunk> chunks,
            List<ExamScopeNode> scopeNodes,
            int topN
    ) {
        validateInput(chunks, scopeNodes, topN);

        documentEmbeddingService.ensureChunkEmbeddings(chunks);
        documentEmbeddingService.ensureScopeNodeEmbeddings(scopeNodes);

        String embeddingModel = embeddingModelProvider.getEmbeddingModel();

        List<Long> chunkIds = extractChunkIds(chunks);
        List<Long> scopeNodeIds = extractScopeNodeIds(scopeNodes);

        Map<Long, DocumentChunkEmbedding> chunkEmbeddingMap =
                getChunkEmbeddingMap(chunkIds, embeddingModel);

        Map<Long, ExamScopeNodeEmbedding> scopeEmbeddingMap =
                getScopeEmbeddingMap(scopeNodeIds, embeddingModel);

        Map<Long, List<ScopeCandidateSearchResultDto>> results = new LinkedHashMap<>();

        for (DocumentChunk chunk : chunks) {
            if (chunk == null || chunk.getId() == null) {
                continue;
            }

            DocumentChunkEmbedding chunkEmbedding = chunkEmbeddingMap.get(chunk.getId());

            if (chunkEmbedding == null) {
                log.warn(
                        "문서 청크 임베딩이 없어 후보 추출에서 제외합니다. documentChunkId={}",
                        chunk.getId()
                );
                continue;
            }

            List<ScopeCandidateSearchResultDto> topCandidates = findTopCandidatesForSingleChunk(
                    chunk,
                    chunkEmbedding,
                    scopeNodes,
                    scopeEmbeddingMap,
                    topN
            );

            results.put(chunk.getId(), topCandidates);
        }

        log.info(
                "청크별 목차 후보 TopN 추출 완료. chunkCount={}, scopeNodeCount={}, resultChunkCount={}, topN={}, embeddingModel={}",
                chunks.size(),
                scopeNodes.size(),
                results.size(),
                topN,
                embeddingModel
        );

        return results;
    }

    private List<ScopeCandidateSearchResultDto> findTopCandidatesForSingleChunk(
            DocumentChunk chunk,
            DocumentChunkEmbedding chunkEmbedding,
            List<ExamScopeNode> scopeNodes,
            Map<Long, ExamScopeNodeEmbedding> scopeEmbeddingMap,
            int topN
    ) {
        List<Double> chunkVector = embeddingJsonConverter.fromJson(chunkEmbedding.getEmbeddingJson());

        List<ScopeCandidateScore> candidateScores = new ArrayList<>();

        for (ExamScopeNode scopeNode : scopeNodes) {
            if (scopeNode == null || scopeNode.getId() == null) {
                continue;
            }

            ExamScopeNodeEmbedding scopeEmbedding = scopeEmbeddingMap.get(scopeNode.getId());

            if (scopeEmbedding == null) {
                continue;
            }

            List<Double> scopeVector = embeddingJsonConverter.fromJson(scopeEmbedding.getEmbeddingJson());

            double similarityScore = cosineSimilarityCalculator.calculate(chunkVector, scopeVector);

            candidateScores.add(new ScopeCandidateScore(
                    scopeNode,
                    similarityScore
            ));
        }

        List<ScopeCandidateScore> topScores = candidateScores.stream()
                .sorted(Comparator.comparingDouble(ScopeCandidateScore::similarityScore).reversed())
                .limit(topN)
                .toList();

        List<ScopeCandidateSearchResultDto> results = new ArrayList<>();

        for (int i = 0; i < topScores.size(); i++) {
            ScopeCandidateScore score = topScores.get(i);

            results.add(new ScopeCandidateSearchResultDto(
                    chunk.getId(),
                    score.examScopeNode(),
                    score.similarityScore(),
                    i + 1
            ));
        }

        return results;
    }

    private Map<Long, DocumentChunkEmbedding> getChunkEmbeddingMap(
            List<Long> chunkIds,
            String embeddingModel
    ) {
        if (chunkIds.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        Map<Long, DocumentChunkEmbedding> embeddingMap =
                documentChunkEmbeddingRepository
                        .findAllByDocumentChunk_IdInAndEmbeddingModel(chunkIds, embeddingModel)
                        .stream()
                        .collect(Collectors.toMap(
                                embedding -> embedding.getDocumentChunk().getId(),
                                Function.identity()
                        ));

        if (embeddingMap.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        return embeddingMap;
    }

    private Map<Long, ExamScopeNodeEmbedding> getScopeEmbeddingMap(
            List<Long> scopeNodeIds,
            String embeddingModel
    ) {
        if (scopeNodeIds.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        Map<Long, ExamScopeNodeEmbedding> embeddingMap =
                examScopeNodeEmbeddingRepository
                        .findAllByExamScopeNode_IdInAndEmbeddingModel(scopeNodeIds, embeddingModel)
                        .stream()
                        .collect(Collectors.toMap(
                                embedding -> embedding.getExamScopeNode().getId(),
                                Function.identity()
                        ));

        if (embeddingMap.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        return embeddingMap;
    }

    private List<Long> extractChunkIds(List<DocumentChunk> chunks) {
        return chunks.stream()
                .filter(Objects::nonNull)
                .map(DocumentChunk::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Long> extractScopeNodeIds(List<ExamScopeNode> scopeNodes) {
        return scopeNodes.stream()
                .filter(Objects::nonNull)
                .map(ExamScopeNode::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private void validateInput(
            List<DocumentChunk> chunks,
            List<ExamScopeNode> scopeNodes,
            int topN
    ) {
        if (chunks == null || chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        if (scopeNodes == null || scopeNodes.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        if (topN <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private record ScopeCandidateScore(
            ExamScopeNode examScopeNode,
            double similarityScore
    ) {
    }
}