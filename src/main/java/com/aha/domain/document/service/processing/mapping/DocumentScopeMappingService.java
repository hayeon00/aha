package com.aha.domain.document.service.processing.mapping;

import com.aha.domain.document.client.mapping.DocumentScopeMappingClient;
import com.aha.domain.document.client.mapping.dto.ChunkMappingRequest;
import com.aha.domain.document.client.mapping.dto.ChunkScopeMappingRequest;
import com.aha.domain.document.client.mapping.dto.ScopeCandidateRequest;
import com.aha.domain.document.client.mapping.dto.ScopeMappingAiResult;
import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.DocumentScopeMapping;
import com.aha.domain.document.enums.DocumentScopeMappingMethod;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.learningnote.entity.LearningNote;
import com.aha.domain.learningnote.repository.LearningNoteRepository;
import com.aha.domain.document.service.processing.embedding.similarity.ScopeCandidateSimilarityService;
import com.aha.domain.document.service.processing.embedding.similarity.model.ScopeCandidateSearchResult;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentScopeMappingService {

    private static final double MIN_VECTOR_SIMILARITY = 0.6;
    private static final BigDecimal MIN_MAPPING_CONFIDENCE = BigDecimal.valueOf(0.6);
    private static final int MAX_MAPPING_REASON_LENGTH = 1000;
    private static final int CHUNK_MAPPING_BATCH_SIZE = 5;
    private static final int TOP_SCOPE_CANDIDATE_COUNT = 5;

    private final LearningNoteRepository learningNoteRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final DocumentScopeMappingPersistenceService documentScopeMappingPersistenceService;
    private final DocumentScopeMappingClient documentScopeMappingClient;
    private final ScopeCandidateSimilarityService scopeCandidateSimilarityService;

    public void mapDocuments(Long learningNoteId) {
        validateLearningNoteId(learningNoteId);

        LearningNote learningNote = learningNoteRepository.findByIdWithExamVersion(learningNoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_NOTE_NOT_FOUND));

        Long examVersionId = getExamVersionId(learningNote);

        List<DocumentChunk> chunks = getDocumentChunks(learningNoteId);

        List<ExamScopeNode> scopeNodes = getMappingTargetScopeNodes(examVersionId);

        Map<Long, DocumentChunk> chunkMap = chunks.stream()
                .collect(Collectors.toMap(DocumentChunk::getId, Function.identity()));

        Map<Long, ExamScopeNode> scopeNodeMap = scopeNodes.stream()
                .collect(Collectors.toMap(ExamScopeNode::getId, Function.identity()));

        Map<Long, List<ScopeCandidateSearchResult>> topCandidatesByChunkId =
                scopeCandidateSimilarityService.findTopCandidatesByChunk(
                        chunks,
                        scopeNodes,
                        TOP_SCOPE_CANDIDATE_COUNT
                );

        List<ChunkScopeMappingRequest> mappingRequests =
                buildChunkScopeMappingRequests(chunks, topCandidatesByChunkId);

        List<ScopeMappingAiResult> aiResults =
                requestAiMappingsInBatches(mappingRequests);

        List<DocumentScopeMapping> mappings = createMappings(
                aiResults,
                chunkMap,
                scopeNodeMap,
                topCandidatesByChunkId
        );

        documentScopeMappingPersistenceService.replaceMappings(learningNoteId, mappings);
        updateChunkMappingStatuses(
                chunks,
                mappings
        );
        documentChunkRepository.saveAll(chunks);

        if (mappings.isEmpty()) {
            log.info(
                    "문서에서 신뢰할 수 있는 목차 매핑을 찾지 못했습니다. learningNoteId={}, chunkCount={}",
                    learningNoteId,
                    chunks.size()
            );
            return;
        }

        log.info(
                "문서 청크 목차 매핑 저장 완료. learningNoteId={}, chunkCount={}, mappingCount={}",
                learningNoteId,
                chunks.size(),
                mappings.size()
        );
    }

    private List<ChunkScopeMappingRequest> buildChunkScopeMappingRequests(
            List<DocumentChunk> chunks,
            Map<Long, List<ScopeCandidateSearchResult>> topCandidatesByChunkId
    ) {
        List<ChunkScopeMappingRequest> requests = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            if (chunk == null || chunk.getId() == null) {
                continue;
            }

            List<ScopeCandidateSearchResult> topCandidates =
                    topCandidatesByChunkId.getOrDefault(chunk.getId(), List.of());

            if (topCandidates.isEmpty()) {
                log.warn(
                        "청크에 대한 Top 목차 후보가 없어 AI 매핑 요청에서 제외합니다. documentChunkId={}",
                        chunk.getId()
                );
                continue;
            }

            List<ScopeCandidateRequest> scopeCandidates = topCandidates.stream()
                    .filter(Objects::nonNull)
                    .filter(candidate -> candidate.similarityScore() >= MIN_VECTOR_SIMILARITY)
                    .filter(candidate -> candidate.examScopeNode() != null)
                    .map(this::toScopeCandidateRequest)
                    .toList();

            if (scopeCandidates.isEmpty()) {
                log.warn(
                        "청크에 대한 유효한 목차 후보가 없어 AI 매핑 요청에서 제외합니다. documentChunkId={}",
                        chunk.getId()
                );
                continue;
            }

            requests.add(new ChunkScopeMappingRequest(
                    toChunkMappingRequest(chunk),
                    scopeCandidates
            ));
        }

        return requests;
    }

    private List<ScopeMappingAiResult> requestAiMappingsInBatches(
            List<ChunkScopeMappingRequest> mappingRequests
    ) {
        List<ScopeMappingAiResult> aiResults = new ArrayList<>();

        if (mappingRequests.isEmpty()) {
            return aiResults;
        }

        for (int start = 0; start < mappingRequests.size(); start += CHUNK_MAPPING_BATCH_SIZE) {
            int end = Math.min(start + CHUNK_MAPPING_BATCH_SIZE, mappingRequests.size());
            List<ChunkScopeMappingRequest> requestBatch = mappingRequests.subList(start, end);

            int candidateCount = requestBatch.stream()
                    .mapToInt(request -> request.scopeCandidates() == null ? 0 : request.scopeCandidates().size())
                    .sum();

            log.info(
                    "문서 청크 목차 매핑 AI 배치 요청. start={}, end={}, batchSize={}, totalCandidateCount={}",
                    start,
                    end,
                    requestBatch.size(),
                    candidateCount
            );

            List<ScopeMappingAiResult> batchResults =
                    documentScopeMappingClient.mapChunks(requestBatch);

            if (batchResults == null) {
                throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
            }

            aiResults.addAll(batchResults);
        }

        return aiResults;
    }

    private ChunkMappingRequest toChunkMappingRequest(DocumentChunk documentChunk) {
        if (documentChunk == null
                || documentChunk.getId() == null
                || documentChunk.getContentText() == null
                || documentChunk.getContentText().isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        return new ChunkMappingRequest(
                documentChunk.getId(),
                documentChunk.getChunkOrder(),
                documentChunk.getSectionTitle(),
                documentChunk.getHeadingPath(),
                documentChunk.getContentType() == null ? null : documentChunk.getContentType().name(),
                documentChunk.getCodeLanguage() == null ? null : documentChunk.getCodeLanguage(),
                documentChunk.getContentText()
        );
    }

    private ScopeCandidateRequest toScopeCandidateRequest(ScopeCandidateSearchResult candidate) {
        ExamScopeNode scopeNode = candidate.examScopeNode();
        if (scopeNode == null
                || scopeNode.getId() == null
                || scopeNode.getTitle() == null
                || scopeNode.getTitle().isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        return new ScopeCandidateRequest(
                scopeNode.getId(),
                scopeNode.getCode(),
                scopeNode.getTitle(),
                scopeNode.getDescription(),
                scopeNode.getKeywordsJson(),
                candidate.similarityScore()
        );
    }

    private List<DocumentScopeMapping> createMappings(
            List<ScopeMappingAiResult> aiResults,
            Map<Long, DocumentChunk> chunkMap,
            Map<Long, ExamScopeNode> scopeNodeMap,
            Map<Long, List<ScopeCandidateSearchResult>> topCandidatesByChunkId
    ) {
        if (aiResults == null || aiResults.isEmpty()) {
            return List.of();
        }

        List<DocumentScopeMapping> mappings = new ArrayList<>(aiResults.size());
        Set<String> mappingKeys = new HashSet<>();
        Map<Long, Integer> rankNoByChunkId = new HashMap<>();

        for (ScopeMappingAiResult aiResult : aiResults) {
            if (aiResult == null
                    || aiResult.documentChunkId() == null
                    || aiResult.examScopeNodeId() == null) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
            }

            DocumentChunk documentChunk = chunkMap.get(aiResult.documentChunkId());
            ExamScopeNode examScopeNode = scopeNodeMap.get(aiResult.examScopeNodeId());
            BigDecimal rerankerScore = normalizeConfidenceScore(aiResult.confidenceScore());
            BigDecimal vectorScore = findVectorScore(
                    aiResult.documentChunkId(),
                    aiResult.examScopeNodeId(),
                    topCandidatesByChunkId
            );

            if (vectorScore.compareTo(BigDecimal.valueOf(MIN_VECTOR_SIMILARITY)) < 0) {
                log.warn(
                        "AI가 임계값을 통과한 후보 밖의 목차를 반환하여 제외합니다. documentChunkId={}, examScopeNodeId={}",
                        aiResult.documentChunkId(),
                        aiResult.examScopeNodeId()
                );
                continue;
            }

            BigDecimal confidenceScore = combineScores(vectorScore, rerankerScore);

            if (documentChunk == null) {
                log.warn(
                        "AI가 존재하지 않는 문서 청크 ID를 반환하여 매핑에서 제외합니다. documentChunkId={}",
                        aiResult.documentChunkId()
                );
                continue;
            }

            if (examScopeNode == null) {
                log.warn(
                        "AI가 매핑 후보에 없는 목차 ID를 반환하여 제외합니다. documentChunkId={}, examScopeNodeId={}",
                        aiResult.documentChunkId(),
                        aiResult.examScopeNodeId()
                );
                continue;
            }

            if (confidenceScore.compareTo(MIN_MAPPING_CONFIDENCE) < 0) {
                log.warn(
                        "신뢰도 기준 미만의 목차 매핑을 제외합니다. documentChunkId={}, examScopeNodeId={}, confidenceScore={}",
                        aiResult.documentChunkId(),
                        aiResult.examScopeNodeId(),
                        confidenceScore
                );
                continue;
            }

            String mappingKey = aiResult.documentChunkId() + ":" + aiResult.examScopeNodeId();

            if (!mappingKeys.add(mappingKey)) {
                log.warn(
                        "중복된 목차 매핑 결과를 제외합니다. documentChunkId={}, examScopeNodeId={}",
                        aiResult.documentChunkId(),
                        aiResult.examScopeNodeId()
                );
                continue;
            }

            int rankNo = rankNoByChunkId.merge(
                    documentChunk.getId(),
                    1,
                    Integer::sum
            );

            DocumentScopeMapping mapping =
                    DocumentScopeMapping.create(
                            documentChunk,
                            examScopeNode,
                            rankNo,
                            confidenceScore,
                            DocumentScopeMappingMethod.HYBRID,
                            normalizeMappingReason(
                                    aiResult.mappingReason()
                            )
                    );

            mappings.add(mapping);
        }

        return mappings;
    }

    private BigDecimal findVectorScore(
            Long chunkId,
            Long scopeNodeId,
            Map<Long, List<ScopeCandidateSearchResult>> candidatesByChunkId
    ) {
        return candidatesByChunkId.getOrDefault(chunkId, List.of()).stream()
                .filter(candidate -> candidate.examScopeNode() != null)
                .filter(candidate -> Objects.equals(candidate.examScopeNode().getId(), scopeNodeId))
                .findFirst()
                .map(candidate -> BigDecimal.valueOf(candidate.similarityScore()))
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal combineScores(BigDecimal vectorScore, BigDecimal rerankerScore) {
        return vectorScore.multiply(BigDecimal.valueOf(0.35))
                .add(rerankerScore.multiply(BigDecimal.valueOf(0.65)))
                .setScale(4, java.math.RoundingMode.HALF_UP);
    }

    private void updateChunkMappingStatuses(
            List<DocumentChunk> chunks,
            List<DocumentScopeMapping> mappings
    ) {
        Set<Long> mappedChunkIds = mappings.stream()
                .map(mapping -> mapping.getDocumentChunk().getId())
                .collect(Collectors.toSet());

        for (DocumentChunk chunk : chunks) {
            if (mappedChunkIds.contains(chunk.getId())) {
                chunk.markMapped();
                continue;
            }

            chunk.markUnassigned();
        }
    }

    private String normalizeMappingReason(String mappingReason) {
        if (mappingReason == null || mappingReason.isBlank()) {
            return null;
        }

        String normalizedReason = mappingReason.trim();

        if (normalizedReason.length() > MAX_MAPPING_REASON_LENGTH) {
            return normalizedReason.substring(0, MAX_MAPPING_REASON_LENGTH);
        }

        return normalizedReason;
    }

    private BigDecimal normalizeConfidenceScore(BigDecimal confidenceScore) {
        if (confidenceScore == null
                || confidenceScore.compareTo(BigDecimal.ZERO) < 0
                || confidenceScore.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }

        return confidenceScore.setScale(4, java.math.RoundingMode.HALF_UP);
    }

    private List<ExamScopeNode> getMappingTargetScopeNodes(Long examVersionId) {
        List<ExamScopeNode> scopeNodes =
                examScopeNodeRepository.findAllByExamVersion_IdAndIsLeafTrueAndIsActiveTrueOrderByDepthAscDisplayOrderAsc(examVersionId);

        if (scopeNodes.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        return scopeNodes;
    }

    private List<DocumentChunk> getDocumentChunks(Long learningNoteId) {
        List<DocumentChunk> chunks = documentChunkRepository.findAllByLearningNoteId(learningNoteId);

        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        return chunks;
    }

    private Long getExamVersionId(LearningNote learningNote) {
        if (learningNote.getUserExam() == null
                || learningNote.getUserExam().getExamVersion() == null
                || learningNote.getUserExam().getExamVersion().getId() == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        return learningNote.getUserExam().getExamVersion().getId();
    }

    private void validateLearningNoteId(Long learningNoteId) {
        if (learningNoteId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
