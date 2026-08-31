package com.aha.domain.document.service.processing.mapping;

import com.aha.domain.document.client.mapping.dto.ChunkMappingRequest;
import com.aha.domain.document.client.mapping.dto.ChunkScopeMappingRequest;
import com.aha.domain.document.client.mapping.dto.ScopeCandidateRequest;
import com.aha.domain.document.client.mapping.dto.ScopeMappingAiResult;
import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.DocumentScopeMapping;
import com.aha.domain.document.enums.DocumentScopeMappingMethod;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.document.service.processing.embedding.similarity.ScopeCandidateRetriever;
import com.aha.domain.document.service.processing.embedding.similarity.model.ScopeCandidateSearchResult;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.learningnote.entity.LearningNote;
import com.aha.domain.learningnote.repository.LearningNoteRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentScopeMappingService {

    /*
     * Candidate retrieval 단계의 threshold는 "정답 확정" 기준이 아니라
     * 명백하게 관련 없는 후보만 줄이는 recall-oriented gate다.
     *
     * 실제 값은 사용하는 embedding model + 검증 데이터셋으로 조정해야 한다.
     */
    private static final double MIN_CANDIDATE_SIMILARITY = 0.40;

    /*
     * Top-1과 크게 차이 나지 않는 후보만 AI reranker에 전달한다.
     * 점수가 애매한 후보는 살리되, 지나치게 먼 후보는 제거한다.
     */
    private static final double MAX_SIMILARITY_GAP_FROM_BEST = 0.25;

    private static final BigDecimal MIN_RERANKER_CONFIDENCE =
            BigDecimal.valueOf(0.50);

    private static final BigDecimal EXACT_HEADING_CONFIDENCE =
            BigDecimal.valueOf(1.0);

    private static final int MAX_MAPPING_REASON_LENGTH = 1000;
    private static final int TOP_SCOPE_CANDIDATE_COUNT = 8;

    private final LearningNoteRepository learningNoteRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final DocumentScopeMappingPersistenceService persistenceService;
    private final DocumentScopeMappingBatchProcessor mappingBatchProcessor;
    private final ScopeCandidateRetriever scopeCandidateRetriever;
    private final SemanticScopeMappingFastPath semanticFastPath;

    public void mapDocuments(
            Long learningNoteId
    ) {
        validateLearningNoteId(learningNoteId);

        LearningNote learningNote =
                learningNoteRepository
                        .findByIdWithExamVersion(learningNoteId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.LEARNING_NOTE_NOT_FOUND
                                )
                        );

        Long examVersionId =
                getExamVersionId(learningNote);

        List<DocumentChunk> chunks =
                getDocumentChunks(learningNoteId);

        List<ExamScopeNode> scopeNodes =
                getMappingTargetScopeNodes(examVersionId);

        Map<Long, DocumentChunk> chunkMap =
                chunks.stream()
                        .collect(Collectors.toMap(
                                DocumentChunk::getId,
                                Function.identity()
                        ));

        Map<Long, ExamScopeNode> scopeNodeMap =
                scopeNodes.stream()
                        .collect(Collectors.toMap(
                                ExamScopeNode::getId,
                                Function.identity()
                        ));

        /*
         * 1. Fast path
         *
         * Parser가 sectionTitle을 안정적으로 보존하고 있으므로,
         * sectionTitle과 시험 leaf Topic 제목이 정확하게 일치하고
         * 그 제목이 시험 범위 내에서 유일하다면 AI 호출 없이 확정한다.
         */
        ExactMatchResult exactMatchResult =
                createExactHeadingMappings(
                        chunks,
                        scopeNodes
                );

        List<DocumentChunk> unresolvedChunks =
                chunks.stream()
                        .filter(chunk ->
                                !exactMatchResult.mappedChunkIds()
                                        .contains(chunk.getId())
                        )
                        .toList();

        log.info(
                "목차 매핑 Fast Path 완료. learningNoteId={}, totalChunkCount={}, exactMappedChunkCount={}, unresolvedChunkCount={}",
                learningNoteId,
                chunks.size(),
                exactMatchResult.mappedChunkIds().size(),
                unresolvedChunks.size()
        );

        List<DocumentScopeMapping> semanticMappings = List.of();
        List<DocumentScopeMapping> aiMappings = List.of();

        if (!unresolvedChunks.isEmpty()) {

            /*
             * 2. Candidate retrieval
             *
             * 정확히 일치하지 않는 chunk만 embedding similarity 검색을 수행한다.
             */
            Map<Long, List<ScopeCandidateSearchResult>> candidatesByChunkId =
                    scopeCandidateRetriever.retrieve(
                            examVersionId,
                            unresolvedChunks,
                            scopeNodes,
                            TOP_SCOPE_CANDIDATE_COUNT
                    );

            /*
             * 후보 검증/조회에서 매번 stream 순회하지 않도록
             * chunkId -> scopeId -> vectorScore 형태로 한번 인덱싱한다.
             */
            Map<Long, Map<Long, Double>> candidateScoreIndex =
                    buildCandidateScoreIndex(
                            candidatesByChunkId
                    );

            SemanticScopeMappingFastPath.Result semanticResult =
                    semanticFastPath.evaluate(
                            unresolvedChunks,
                            candidatesByChunkId
                    );
            semanticMappings = semanticResult.mappings();

            List<DocumentChunk> aiTargetChunks = unresolvedChunks.stream()
                    .filter(chunk -> !semanticResult.mappedChunkIds().contains(chunk.getId()))
                    .toList();

            log.info(
                    "[DOCUMENT_PERF] semantic mapping fast path completed. learningNoteId={}, unresolvedChunkCount={}, semanticMappedChunkCount={}, aiTargetChunkCount={}",
                    learningNoteId,
                    unresolvedChunks.size(),
                    semanticResult.mappedChunkIds().size(),
                    aiTargetChunks.size()
            );

            List<ChunkScopeMappingRequest> mappingRequests =
                    buildChunkScopeMappingRequests(
                            aiTargetChunks,
                            candidatesByChunkId
                    );

            /*
             * 3. AI reranking/classification
             */
            List<ScopeMappingAiResult> aiResults =
                    mappingBatchProcessor.process(
                            mappingRequests
                    );

            aiMappings =
                    createAiMappings(
                            aiResults,
                            chunkMap,
                            scopeNodeMap,
                            candidateScoreIndex
                    );
        }

        List<DocumentScopeMapping> finalMappings =
                new ArrayList<>(
                        exactMatchResult.mappings().size()
                                + semanticMappings.size()
                                + aiMappings.size()
                );

        finalMappings.addAll(
                exactMatchResult.mappings()
        );

        finalMappings.addAll(
                semanticMappings
        );

        finalMappings.addAll(
                aiMappings
        );

        /*
         * 외부 AI 호출이 모두 끝난 후에 DB 쓰기 transaction을 시작한다.
         * 매핑 교체 + chunk 상태 변경을 하나의 transaction으로 처리한다.
         */
        persistenceService.replaceMappingsAndUpdateChunkStatuses(
                learningNoteId,
                chunks,
                finalMappings
        );

        log.info(
                "문서 목차 매핑 완료. learningNoteId={}, chunkCount={}, exactMappingCount={}, semanticMappingCount={}, aiMappingCount={}, totalMappingCount={}",
                learningNoteId,
                chunks.size(),
                exactMatchResult.mappings().size(),
                semanticMappings.size(),
                aiMappings.size(),
                finalMappings.size()
        );
    }


    /**
     * Section 제목이 시험 leaf Topic 제목과 정확히 일치하는 경우의 Fast Path.
     *
     * 동일한 normalized title을 가진 Topic이 둘 이상이면 모호하므로
     * 자동 확정하지 않고 semantic + AI 단계로 넘긴다.
     */
    private ExactMatchResult createExactHeadingMappings(
            List<DocumentChunk> chunks,
            List<ExamScopeNode> scopeNodes
    ) {
        Map<String, List<ExamScopeNode>> nodesByNormalizedTitle =
                scopeNodes.stream()
                        .filter(Objects::nonNull)
                        .filter(node ->
                                node.getId() != null
                                        && node.getTitle() != null
                                        && !node.getTitle().isBlank()
                        )
                        .collect(Collectors.groupingBy(
                                node ->
                                        normalizeTopicText(
                                                node.getTitle()
                                        )
                        ));

        List<DocumentScopeMapping> mappings =
                new ArrayList<>();

        Set<Long> mappedChunkIds =
                new HashSet<>();

        for (DocumentChunk chunk : chunks) {

            if (chunk == null
                    || chunk.getId() == null) {
                continue;
            }

            String sectionTitle =
                    chunk.getSectionTitle();

            if (sectionTitle == null
                    || sectionTitle.isBlank()) {
                continue;
            }

            String normalizedSectionTitle =
                    normalizeTopicText(
                            sectionTitle
                    );

            if (normalizedSectionTitle.isBlank()) {
                continue;
            }

            List<ExamScopeNode> matchedNodes =
                    nodesByNormalizedTitle.getOrDefault(
                            normalizedSectionTitle,
                            List.of()
                    );

            /*
             * 정확히 하나의 leaf Topic과만 일치할 때만 확정.
             */
            if (matchedNodes.size() != 1) {
                continue;
            }

            ExamScopeNode matchedNode =
                    matchedNodes.get(0);

            DocumentScopeMapping mapping =
                    DocumentScopeMapping.create(
                            chunk,
                            matchedNode,
                            1,
                            EXACT_HEADING_CONFIDENCE,
                            DocumentScopeMappingMethod.EXACT_TITLE,
                            "문서 Section 제목과 시험 Topic 제목이 정확히 일치함"
                    );

            mappings.add(mapping);
            mappedChunkIds.add(
                    chunk.getId()
            );
        }

        return new ExactMatchResult(
                List.copyOf(mappings),
                Set.copyOf(mappedChunkIds)
        );
    }

    private List<ChunkScopeMappingRequest> buildChunkScopeMappingRequests(
            List<DocumentChunk> chunks,
            Map<Long, List<ScopeCandidateSearchResult>> candidatesByChunkId
    ) {
        List<ChunkScopeMappingRequest> requests =
                new ArrayList<>();

        for (DocumentChunk chunk : chunks) {

            if (chunk == null
                    || chunk.getId() == null) {
                continue;
            }

            List<ScopeCandidateSearchResult> candidates =
                    candidatesByChunkId.getOrDefault(
                            chunk.getId(),
                            List.of()
                    );

            List<ScopeCandidateRequest> selectedCandidates =
                    selectCandidateRequests(
                            candidates
                    );

            if (selectedCandidates.isEmpty()) {

                log.info(
                        "유효한 목차 후보가 없어 AI 매핑 요청에서 제외합니다. documentChunkId={}",
                        chunk.getId()
                );

                continue;
            }

            requests.add(
                    new ChunkScopeMappingRequest(
                            toChunkMappingRequest(chunk),
                            selectedCandidates
                    )
            );
        }

        return List.copyOf(requests);
    }

    /**
     * 고정 0.6 threshold 하나로 잘라버리지 않고:
     *
     * 1. 최소 안전선 통과
     * 2. Top-1 대비 일정 범위 안에 있는 후보 유지
     *
     * 방식으로 recall을 우선 확보한다.
     */
    private List<ScopeCandidateRequest> selectCandidateRequests(
            List<ScopeCandidateSearchResult> candidates
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<ScopeCandidateSearchResult> validCandidates =
                candidates.stream()
                        .filter(Objects::nonNull)
                        .filter(candidate ->
                                candidate.examScopeNode() != null
                        )
                        .sorted(
                                Comparator.comparingDouble(
                                        ScopeCandidateSearchResult::similarityScore
                                ).reversed()
                        )
                        .toList();

        if (validCandidates.isEmpty()) {
            return List.of();
        }

        double bestScore =
                validCandidates.get(0).similarityScore();

        List<ScopeCandidateRequest> selected =
                validCandidates.stream()
                        .filter(candidate ->
                                candidate.similarityScore()
                                        >= MIN_CANDIDATE_SIMILARITY
                        )
                        .filter(candidate ->
                                bestScore
                                        - candidate.similarityScore()
                                        <= MAX_SIMILARITY_GAP_FROM_BEST
                        )
                        .map(this::toScopeCandidateRequest)
                        .toList();

        selected.forEach(candidate ->
                log.info(
                        "[AI CANDIDATE] scopeNodeId={}, title={}, similarity={}",
                        candidate.examScopeNodeId(),
                        candidate.title(),
                        candidate.similarityScore()
                )
        );

        return selected;
    }

    /**
     * AI 결과를 candidate set 안에서만 허용하고,
     * chunk별 confidence 내림차순으로 정렬한 뒤 rank를 부여한다.
     *
     * Vector similarity와 AI confidence는 서로 다른 score space이므로
     * 임의의 0.35/0.65 선형 결합을 하지 않는다.
     * Vector score는 retrieval gate,
     * AI confidence는 최종 reranking confidence로 역할을 분리한다.
     */
    private List<DocumentScopeMapping> createAiMappings(
            List<ScopeMappingAiResult> aiResults,
            Map<Long, DocumentChunk> chunkMap,
            Map<Long, ExamScopeNode> scopeNodeMap,
            Map<Long, Map<Long, Double>> candidateScoreIndex
    ) {
        if (aiResults == null
                || aiResults.isEmpty()) {
            return List.of();
        }

        Map<Long, List<ValidatedAiMapping>> validatedByChunkId =
                new LinkedHashMap<>();

        Set<String> deduplicationKeys =
                new HashSet<>();

        for (ScopeMappingAiResult aiResult : aiResults) {

            if (aiResult == null
                    || aiResult.documentChunkId() == null
                    || aiResult.examScopeNodeId() == null) {

                throw new BusinessException(
                        ErrorCode.AI_RESPONSE_PARSE_FAILED
                );
            }

            DocumentChunk chunk =
                    chunkMap.get(
                            aiResult.documentChunkId()
                    );

            ExamScopeNode scopeNode =
                    scopeNodeMap.get(
                            aiResult.examScopeNodeId()
                    );

            if (chunk == null
                    || scopeNode == null) {

                log.warn(
                        "AI 응답에 유효하지 않은 ID가 포함되어 제외합니다. documentChunkId={}, examScopeNodeId={}",
                        aiResult.documentChunkId(),
                        aiResult.examScopeNodeId()
                );

                continue;
            }

            Double vectorScore =
                    candidateScoreIndex
                            .getOrDefault(
                                    chunk.getId(),
                                    Map.of()
                            )
                            .get(
                                    scopeNode.getId()
                            );

            /*
             * AI가 실제로 제공된 candidate 밖의 Topic을 반환하면 hallucination으로 간주.
             */
            if (vectorScore == null) {

                log.warn(
                        "AI가 candidate set 밖의 Topic을 반환하여 제외합니다. documentChunkId={}, examScopeNodeId={}",
                        chunk.getId(),
                        scopeNode.getId()
                );

                continue;
            }

            BigDecimal rerankerConfidence =
                    normalizeConfidenceScore(
                            aiResult.confidenceScore()
                    );

            if (rerankerConfidence.compareTo(
                    MIN_RERANKER_CONFIDENCE
            ) < 0) {

                continue;
            }

            String deduplicationKey =
                    chunk.getId()
                            + ":"
                            + scopeNode.getId();

            if (!deduplicationKeys.add(
                    deduplicationKey
            )) {
                continue;
            }

            validatedByChunkId
                    .computeIfAbsent(
                            chunk.getId(),
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            new ValidatedAiMapping(
                                    chunk,
                                    scopeNode,
                                    rerankerConfidence,
                                    vectorScore,
                                    normalizeMappingReason(
                                            aiResult.mappingReason()
                                    )
                            )
                    );
        }

        List<DocumentScopeMapping> mappings =
                new ArrayList<>();

        for (List<ValidatedAiMapping> chunkMappings
                : validatedByChunkId.values()) {

            List<ValidatedAiMapping> rankedMappings =
                    chunkMappings.stream()
                            .sorted(
                                    Comparator
                                            .comparing(
                                                    ValidatedAiMapping::confidenceScore
                                            )
                                            .reversed()
                                            .thenComparing(
                                                    ValidatedAiMapping::vectorScore,
                                                    Comparator.reverseOrder()
                                            )
                            )
                            .toList();

            int rankNo = 1;

            for (ValidatedAiMapping ranked : rankedMappings) {

                mappings.add(
                        DocumentScopeMapping.create(
                                ranked.chunk(),
                                ranked.scopeNode(),
                                rankNo++,
                                ranked.confidenceScore(),
                                DocumentScopeMappingMethod.HYBRID,
                                ranked.mappingReason()
                        )
                );
            }
        }

        return List.copyOf(mappings);
    }

    private Map<Long, Map<Long, Double>> buildCandidateScoreIndex(
            Map<Long, List<ScopeCandidateSearchResult>> candidatesByChunkId
    ) {
        Map<Long, Map<Long, Double>> index =
                new HashMap<>();

        for (Map.Entry<Long, List<ScopeCandidateSearchResult>> entry
                : candidatesByChunkId.entrySet()) {

            Map<Long, Double> scoreByScopeNodeId =
                    new HashMap<>();

            for (ScopeCandidateSearchResult candidate
                    : entry.getValue()) {

                if (candidate == null
                        || candidate.examScopeNode() == null
                        || candidate.examScopeNode().getId() == null) {
                    continue;
                }

                scoreByScopeNodeId.put(
                        candidate.examScopeNode().getId(),
                        candidate.similarityScore()
                );
            }

            index.put(
                    entry.getKey(),
                    Map.copyOf(scoreByScopeNodeId)
            );
        }

        return Map.copyOf(index);
    }

    private ChunkMappingRequest toChunkMappingRequest(
            DocumentChunk documentChunk
    ) {
        if (documentChunk == null
                || documentChunk.getId() == null
                || documentChunk.getContentText() == null
                || documentChunk.getContentText().isBlank()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }

        return new ChunkMappingRequest(
                documentChunk.getId(),
                documentChunk.getChunkOrder(),
                documentChunk.getSectionTitle(),
                documentChunk.getHeadingPath(),
                documentChunk.getContentType() == null
                        ? null
                        : documentChunk.getContentType().name(),
                documentChunk.getCodeLanguage() == null
                        ? null
                        : documentChunk.getCodeLanguage(),
                documentChunk.getContentText()
        );
    }

    private ScopeCandidateRequest toScopeCandidateRequest(
            ScopeCandidateSearchResult candidate
    ) {
        ExamScopeNode scopeNode =
                candidate.examScopeNode();

        if (scopeNode == null
                || scopeNode.getId() == null
                || scopeNode.getTitle() == null
                || scopeNode.getTitle().isBlank()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
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

    private String normalizeTopicText(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT)

                // Markdown heading 제거
                .replaceFirst(
                        "^#{1,6}\\s*",
                        ""
                )

                // "제 1 과목", "제2장", "제3절" 등 제거
                .replaceFirst(
                        "^제\\s*\\d+\\s*(?:장|절|과목)\\s*",
                        ""
                )

                // "1.", "1.1", "1.1.1)" 등 번호 prefix 제거
                .replaceFirst(
                        "^\\d+(?:\\.\\d+)*[.)]?\\s*",
                        ""
                )

                // 공백/구두점 제거, 한글/영문/숫자만 비교
                .replaceAll(
                        "[^0-9a-z가-힣]",
                        ""
                );
    }

    private String normalizeMappingReason(
            String mappingReason
    ) {
        if (mappingReason == null
                || mappingReason.isBlank()) {
            return null;
        }

        String normalizedReason =
                mappingReason.trim();

        if (normalizedReason.length()
                > MAX_MAPPING_REASON_LENGTH) {

            return normalizedReason.substring(
                    0,
                    MAX_MAPPING_REASON_LENGTH
            );
        }

        return normalizedReason;
    }

    private BigDecimal normalizeConfidenceScore(
            BigDecimal confidenceScore
    ) {
        if (confidenceScore == null
                || confidenceScore.compareTo(
                BigDecimal.ZERO
        ) < 0
                || confidenceScore.compareTo(
                BigDecimal.ONE
        ) > 0) {

            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }

        return confidenceScore.setScale(
                4,
                RoundingMode.HALF_UP
        );
    }

    private List<ExamScopeNode> getMappingTargetScopeNodes(
            Long examVersionId
    ) {
        List<ExamScopeNode> scopeNodes =
                examScopeNodeRepository
                        .findAllByExamVersion_IdAndIsLeafTrueAndIsActiveTrueOrderByDepthAscDisplayOrderAsc(
                                examVersionId
                        );

        if (scopeNodes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        return scopeNodes;
    }

    private List<DocumentChunk> getDocumentChunks(
            Long learningNoteId
    ) {
        List<DocumentChunk> chunks =
                documentChunkRepository
                        .findAllByLearningNoteId(
                                learningNoteId
                        );

        if (chunks.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }

        return chunks;
    }

    private Long getExamVersionId(
            LearningNote learningNote
    ) {
        if (learningNote.getUserExam() == null
                || learningNote.getUserExam().getExamVersion() == null
                || learningNote.getUserExam().getExamVersion().getId() == null) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        return learningNote
                .getUserExam()
                .getExamVersion()
                .getId();
    }

    private void validateLearningNoteId(
            Long learningNoteId
    ) {
        if (learningNoteId == null
                || learningNoteId <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private record ExactMatchResult(
            List<DocumentScopeMapping> mappings,
            Set<Long> mappedChunkIds
    ) {
    }

    private record ValidatedAiMapping(
            DocumentChunk chunk,
            ExamScopeNode scopeNode,
            BigDecimal confidenceScore,
            Double vectorScore,
            String mappingReason
    ) {
    }
}
