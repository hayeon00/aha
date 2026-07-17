package com.aha.domain.ailearn.document.service.mapping;

import com.aha.domain.ailearn.document.client.mapping.DocumentScopeMappingClient;
import com.aha.domain.ailearn.document.dto.mapping.request.ChunkMappingRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.request.ChunkScopeMappingRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.request.ScopeCandidateRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.response.ScopeMappingAiResultResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import com.aha.domain.ailearn.document.repository.DocumentChunkRepository;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.service.embedding.similarity.ScopeCandidateSimilarityService;
import com.aha.domain.ailearn.document.service.embedding.similarity.dto.ScopeCandidateSearchResultDto;
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

    private final DocumentProcessingGroupRepository documentProcessingGroupRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final DocumentScopeMappingPersistenceService documentScopeMappingPersistenceService;
    private final DocumentScopeMappingClient documentScopeMappingClient;
    private final ScopeCandidateSimilarityService scopeCandidateSimilarityService;

    public void mapDocuments(Long processingGroupId) {
        validateProcessingGroupId(processingGroupId);

        DocumentProcessingGroup processingGroup = documentProcessingGroupRepository.findByIdWithExamVersion(processingGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));

        Long examVersionId = getExamVersionId(processingGroup);

        List<DocumentChunk> chunks = getDocumentChunks(processingGroupId);

        List<ExamScopeNode> scopeNodes = getMappingTargetScopeNodes(examVersionId);

        Map<Long, DocumentChunk> chunkMap = chunks.stream()
                .collect(Collectors.toMap(DocumentChunk::getId, Function.identity()));

        Map<Long, ExamScopeNode> scopeNodeMap = scopeNodes.stream()
                .collect(Collectors.toMap(ExamScopeNode::getId, Function.identity()));

        Map<Long, List<ScopeCandidateSearchResultDto>> topCandidatesByChunkId =
                scopeCandidateSimilarityService.findTopCandidatesByChunk(
                        chunks,
                        scopeNodes,
                        TOP_SCOPE_CANDIDATE_COUNT
                );

        List<ChunkScopeMappingRequestDto> mappingRequests =
                buildChunkScopeMappingRequests(chunks, topCandidatesByChunkId);

        List<ScopeMappingAiResultResponseDto> aiResults =
                requestAiMappingsInBatches(mappingRequests);

        List<DocumentScopeMapping> mappings = createMappings(
                aiResults,
                chunkMap,
                scopeNodeMap,
                topCandidatesByChunkId
        );

        documentScopeMappingPersistenceService.replaceMappings(processingGroupId, mappings);
        updateChunkMappingStatuses(chunks, mappings, topCandidatesByChunkId);
        documentChunkRepository.saveAll(chunks);

        if (mappings.isEmpty()) {
            log.info(
                    "문서에서 신뢰할 수 있는 목차 매핑을 찾지 못했습니다. processingGroupId={}, chunkCount={}",
                    processingGroupId,
                    chunks.size()
            );
            return;
        }

        log.info(
                "문서 청크 목차 매핑 저장 완료. processingGroupId={}, chunkCount={}, mappingCount={}",
                processingGroupId,
                chunks.size(),
                mappings.size()
        );
    }

    private List<ChunkScopeMappingRequestDto> buildChunkScopeMappingRequests(
            List<DocumentChunk> chunks,
            Map<Long, List<ScopeCandidateSearchResultDto>> topCandidatesByChunkId
    ) {
        List<ChunkScopeMappingRequestDto> requests = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            if (chunk == null || chunk.getId() == null) {
                continue;
            }

            List<ScopeCandidateSearchResultDto> topCandidates =
                    topCandidatesByChunkId.getOrDefault(chunk.getId(), List.of());

            if (topCandidates.isEmpty()) {
                log.warn(
                        "청크에 대한 Top 목차 후보가 없어 AI 매핑 요청에서 제외합니다. documentChunkId={}",
                        chunk.getId()
                );
                continue;
            }

            List<ScopeCandidateRequestDto> scopeCandidates = topCandidates.stream()
                    .filter(Objects::nonNull)
                    .filter(candidate -> candidate.similarityScore() >= MIN_VECTOR_SIMILARITY)
                    .filter(candidate -> candidate.examScopeNode() != null)
                    .map(this::toScopeCandidateRequestDto)
                    .toList();

            if (scopeCandidates.isEmpty()) {
                log.warn(
                        "청크에 대한 유효한 목차 후보가 없어 AI 매핑 요청에서 제외합니다. documentChunkId={}",
                        chunk.getId()
                );
                continue;
            }

            requests.add(new ChunkScopeMappingRequestDto(
                    toChunkMappingRequestDto(chunk),
                    scopeCandidates
            ));
        }

        return requests;
    }

    private List<ScopeMappingAiResultResponseDto> requestAiMappingsInBatches(
            List<ChunkScopeMappingRequestDto> mappingRequests
    ) {
        List<ScopeMappingAiResultResponseDto> aiResults = new ArrayList<>();

        if (mappingRequests.isEmpty()) {
            return aiResults;
        }

        for (int start = 0; start < mappingRequests.size(); start += CHUNK_MAPPING_BATCH_SIZE) {
            int end = Math.min(start + CHUNK_MAPPING_BATCH_SIZE, mappingRequests.size());
            List<ChunkScopeMappingRequestDto> requestBatch = mappingRequests.subList(start, end);

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

            List<ScopeMappingAiResultResponseDto> batchResults =
                    documentScopeMappingClient.mapChunks(requestBatch);

            if (batchResults == null) {
                throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
            }

            aiResults.addAll(batchResults);
        }

        return aiResults;
    }

    private ChunkMappingRequestDto toChunkMappingRequestDto(DocumentChunk documentChunk) {
        if (documentChunk == null
                || documentChunk.getId() == null
                || documentChunk.getContentText() == null
                || documentChunk.getContentText().isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        return new ChunkMappingRequestDto(
                documentChunk.getId(),
                documentChunk.getChunkOrder(),
                documentChunk.getSectionTitle(),
                documentChunk.getHeadingPath(),
                documentChunk.getContentType() == null ? null : documentChunk.getContentType().name(),
                documentChunk.getCodeLanguage() == null ? null : documentChunk.getCodeLanguage().name(),
                documentChunk.getContentText()
        );
    }

    private ScopeCandidateRequestDto toScopeCandidateRequestDto(ScopeCandidateSearchResultDto candidate) {
        ExamScopeNode scopeNode = candidate.examScopeNode();
        if (scopeNode == null
                || scopeNode.getId() == null
                || scopeNode.getTitle() == null
                || scopeNode.getTitle().isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        return new ScopeCandidateRequestDto(
                scopeNode.getId(),
                scopeNode.getCode(),
                scopeNode.getTitle(),
                scopeNode.getDescription(),
                scopeNode.getKeywordsJson(),
                candidate.similarityScore()
        );
    }

    private List<DocumentScopeMapping> createMappings(
            List<ScopeMappingAiResultResponseDto> aiResults,
            Map<Long, DocumentChunk> chunkMap,
            Map<Long, ExamScopeNode> scopeNodeMap,
            Map<Long, List<ScopeCandidateSearchResultDto>> topCandidatesByChunkId
    ) {
        if (aiResults == null || aiResults.isEmpty()) {
            return List.of();
        }

        List<DocumentScopeMapping> mappings = new ArrayList<>(aiResults.size());
        Set<String> mappingKeys = new HashSet<>();
        Map<Long, Integer> rankNoByChunkId = new HashMap<>();

        for (ScopeMappingAiResultResponseDto aiResult : aiResults) {
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

            DocumentScopeMapping mapping = DocumentScopeMapping.builder()
                    .documentChunk(documentChunk)
                    .examScopeNode(examScopeNode)
                    .confidenceScore(confidenceScore)
                    .rankNo(rankNo)
                    .mappingReason(normalizeMappingReason(aiResult.mappingReason()))
                    .build();

            mappings.add(mapping);
        }

        return mappings;
    }

    private BigDecimal findVectorScore(
            Long chunkId,
            Long scopeNodeId,
            Map<Long, List<ScopeCandidateSearchResultDto>> candidatesByChunkId
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
            List<DocumentScopeMapping> mappings,
            Map<Long, List<ScopeCandidateSearchResultDto>> candidatesByChunkId
    ) {
        Map<Long, BigDecimal> mappedScoreByChunkId = mappings.stream()
                .collect(Collectors.toMap(
                        mapping -> mapping.getDocumentChunk().getId(),
                        DocumentScopeMapping::getConfidenceScore,
                        BigDecimal::max
                ));

        for (DocumentChunk chunk : chunks) {
            BigDecimal mappedScore = mappedScoreByChunkId.get(chunk.getId());
            if (mappedScore != null) {
                chunk.markAutoMapped(mappedScore);
                continue;
            }

            BigDecimal bestVectorScore = candidatesByChunkId
                    .getOrDefault(chunk.getId(), List.of())
                    .stream()
                    .map(candidate -> BigDecimal.valueOf(candidate.similarityScore()))
                    .max(BigDecimal::compareTo)
                    .orElse(null);
            chunk.markUnassigned(bestVectorScore);
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

    private List<DocumentChunk> getDocumentChunks(Long processingGroupId) {
        List<DocumentChunk> chunks = documentChunkRepository.findAllByProcessingGroupId(processingGroupId);

        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        return chunks;
    }

    private Long getExamVersionId(DocumentProcessingGroup processingGroup) {
        if (processingGroup.getUserExam() == null
                || processingGroup.getUserExam().getExamVersion() == null
                || processingGroup.getUserExam().getExamVersion().getId() == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        return processingGroup.getUserExam().getExamVersion().getId();
    }

    private void validateProcessingGroupId(Long processingGroupId) {
        if (processingGroupId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
