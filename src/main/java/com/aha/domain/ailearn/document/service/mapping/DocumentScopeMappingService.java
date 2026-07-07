package com.aha.domain.ailearn.document.service.mapping;

import com.aha.domain.ailearn.document.client.mapping.DocumentScopeMappingClient;
import com.aha.domain.ailearn.document.dto.mapping.request.ChunkMappingRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.request.ScopeCandidateRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.response.ScopeMappingAiResultResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import com.aha.domain.ailearn.document.repository.DocumentChunkRepository;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
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

    private static final BigDecimal MIN_MAPPING_CONFIDENCE = BigDecimal.valueOf(0.7);
    private static final int MAX_MAPPING_REASON_LENGTH = 1000;
    private static final int CHUNK_MAPPING_BATCH_SIZE = 5;

    private final DocumentProcessingGroupRepository documentProcessingGroupRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final DocumentScopeMappingPersistenceService documentScopeMappingPersistenceService;
    private final DocumentScopeMappingClient documentScopeMappingClient;

    public void mapDocuments(Long processingGroupId){

        if(processingGroupId == null){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        DocumentProcessingGroup processingGroup = documentProcessingGroupRepository.findByIdWithExamVersion(processingGroupId)
                .orElseThrow(()-> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));

        // 시험 버전 조회
        Long examVersionId = getExamVersionId(processingGroup);

        // 생성된 청크 조회
        List<DocumentChunk> chunks = getDocumentChunks(processingGroupId);

        // 시험 목차 조회
        List<ExamScopeNode> scopeNodes = getMappingTargetScopeNodes(examVersionId);

        Map<Long, DocumentChunk> chunkMap = chunks.stream()
                .collect(Collectors.toMap(DocumentChunk::getId, Function.identity()));

        Map<Long, ExamScopeNode> scopeNodeMap = scopeNodes.stream()
                .collect(Collectors.toMap(ExamScopeNode::getId, Function.identity()));

        List<ChunkMappingRequestDto> chunkRequests = chunks.stream().map(this::toChunkMappingRequestDto).toList();

        List<ScopeCandidateRequestDto> scopeCandidates = scopeNodes.stream().map(this::toScopeCandidateRequestDto).toList();


        // 청크를 5개씩 쪼개서 ai에게 보냄
        List<ScopeMappingAiResultResponseDto> aiResults = requestAiMappingsInBatches(chunkRequests, scopeCandidates);

        List<DocumentScopeMapping> mappings = createMappings(aiResults, chunkMap, scopeNodeMap);

        documentScopeMappingPersistenceService.replaceMappings(processingGroupId, mappings);

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

    private List<ScopeMappingAiResultResponseDto> requestAiMappingsInBatches(List<ChunkMappingRequestDto> chunkRequests, List<ScopeCandidateRequestDto> scopeCandidates) {

        List<ScopeMappingAiResultResponseDto> aiResults = new ArrayList<>();

        for (int start = 0; start < chunkRequests.size(); start += CHUNK_MAPPING_BATCH_SIZE) {
            int end = Math.min(start + CHUNK_MAPPING_BATCH_SIZE, chunkRequests.size());
            List<ChunkMappingRequestDto> chunkBatch = chunkRequests.subList(start, end);

            log.info(
                    "문서 청크 목차 매핑 AI 배치 요청. start={}, end={}, batchSize={}, scopeCandidateCount={}",
                    start,
                    end,
                    chunkBatch.size(),
                    scopeCandidates.size()
            );

            List<ScopeMappingAiResultResponseDto> batchResults =
                    documentScopeMappingClient.mapChunks(chunkBatch, scopeCandidates);

            if (batchResults == null) {
                throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
            }

            aiResults.addAll(batchResults);
        }

        return aiResults;
    }


    private ChunkMappingRequestDto toChunkMappingRequestDto(DocumentChunk documentChunk) {

        if(documentChunk == null || documentChunk.getId() == null || documentChunk.getContentText() == null || documentChunk.getContentText().isBlank()){
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        return new ChunkMappingRequestDto(documentChunk.getId(), documentChunk.getContentText());

    }

    private ScopeCandidateRequestDto toScopeCandidateRequestDto(ExamScopeNode scopeNode) {

        if(scopeNode == null || scopeNode.getId() == null || scopeNode.getTitle() == null || scopeNode.getTitle().isBlank()){
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        return new ScopeCandidateRequestDto(scopeNode.getId(), scopeNode.getTitle());
    }

    private List<DocumentScopeMapping> createMappings(List<ScopeMappingAiResultResponseDto> aiResults, Map<Long, DocumentChunk> chunkMap, Map<Long, ExamScopeNode> scopeNodeMap) {

        List<DocumentScopeMapping> mappings = new ArrayList<>(aiResults.size());

        Set<String> mappingKeys = new HashSet<>();

        for(ScopeMappingAiResultResponseDto aiResult : aiResults){

            if(aiResult == null || aiResult.documentChunkId() == null || aiResult.examScopeNodeId() == null){
                throw new BusinessException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
            }

            DocumentChunk documentChunk = chunkMap.get(aiResult.documentChunkId());

            ExamScopeNode examScopeNode = scopeNodeMap.get(aiResult.examScopeNodeId());

            BigDecimal confidenceScore = normalizeConfidenceScore(aiResult.confidenceScore());

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

            if(confidenceScore.compareTo(MIN_MAPPING_CONFIDENCE) < 0){
                log.warn(
                        "신뢰도 기준 미만의 목차 매핑을 제외합니다. " +
                                "documentChunkId={}, examScopeNodeId={}, confidenceScore={}",
                        aiResult.documentChunkId(),
                        aiResult.examScopeNodeId(),
                        confidenceScore
                );

                continue;
            }

            String mappingKey = aiResult.documentChunkId() + ":" + aiResult.examScopeNodeId();

            if(!mappingKeys.add(mappingKey)){
                log.warn(
                        "중복된 목차 매핑 결과를 제외합니다. documentChunkId={}, examScopeNodeId={}",
                        aiResult.documentChunkId(),
                        aiResult.examScopeNodeId()
                );

                continue;
            }

            DocumentScopeMapping mapping = DocumentScopeMapping.builder()
                    .documentChunk(documentChunk)
                    .examScopeNode(examScopeNode)
                    .confidenceScore(confidenceScore)
                    .mappingReason(normalizeMappingReason(aiResult.mappingReason()))
                    .build();

            mappings.add(mapping);
        }

        return mappings;

    }

    private String normalizeMappingReason(String mappingReason) {

        if(mappingReason == null || mappingReason.isBlank()){
            return null;
        }

        String normalizedReason = mappingReason.trim();

        if(normalizedReason.length() > MAX_MAPPING_REASON_LENGTH){
            return normalizedReason.substring(0,MAX_MAPPING_REASON_LENGTH);
        }

        return normalizedReason;

    }

    private BigDecimal normalizeConfidenceScore(BigDecimal confidenceScore) {

        if(confidenceScore == null || confidenceScore.compareTo(BigDecimal.ZERO) < 0 || confidenceScore.compareTo(BigDecimal.ONE) > 0){
            throw new BusinessException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }

        return confidenceScore.setScale(4, java.math.RoundingMode.HALF_UP);
    }

    private List<ExamScopeNode> getMappingTargetScopeNodes(Long examVersionId) {

        List<ExamScopeNode> scopeNodes = examScopeNodeRepository.findAllByExamVersion_IdAndIsLeafTrueAndIsActiveTrueOrderByDepthAscDisplayOrderAsc(examVersionId);

        if(scopeNodes.isEmpty()){
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        return scopeNodes;
    }

    private List<DocumentChunk> getDocumentChunks(Long processingGroupId) {

        List<DocumentChunk> chunks = documentChunkRepository.findAllByProcessingGroupId(processingGroupId);

        if(chunks.isEmpty()){
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        return chunks;
    }

    private Long getExamVersionId(DocumentProcessingGroup processingGroup) {

        if(processingGroup.getUserExam() == null || processingGroup.getUserExam().getExamVersion() == null
                        || processingGroup.getUserExam().getExamVersion().getId() == null){

            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        return  processingGroup.getUserExam().getExamVersion().getId();
    }

}