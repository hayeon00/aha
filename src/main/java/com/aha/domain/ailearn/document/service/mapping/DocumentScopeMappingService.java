package com.aha.domain.ailearn.document.service.mapping;

import com.aha.domain.ailearn.document.client.DocumentScopeMappingClient;
import com.aha.domain.ailearn.document.dto.mapping.request.ExamScopeCandidateDto;
import com.aha.domain.ailearn.document.dto.mapping.response.DocumentScopeMappingResultDto;
import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import com.aha.domain.ailearn.document.repository.DocumentChunkRepository;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.DocumentProcessingRepository;
import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.enums.ExamScopeNodeType;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentScopeMappingService {

    private final DocumentProcessingRepository documentProcessingRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentScopeMappingRepository documentScopeMappingRepository;
    private final DocumentProcessingGroupRepository documentProcessingGroupRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final DocumentScopeMappingClient documentScopeMappingClient;

    /**
     * 처리 그룹에 포함된 모든 문서의 청크를
     * 해당 시험 버전의 목차에 매핑한다.
     */
    @Transactional
    public void mapDocuments(
            Long processingGroupId
    ) {
        List<DocumentProcessing> processings =
                getDocumentProcessings(
                        processingGroupId
                );

        List<ExamScopeNode> scopeNodes =
                getExamScopeNodes(
                        processingGroupId
                );

        List<ExamScopeCandidateDto> candidates =
                createScopeCandidates(
                        scopeNodes
                );

        Map<Long, ExamScopeNode> scopeNodeMap =
                scopeNodes.stream()
                        .collect(
                                Collectors.toMap(
                                        ExamScopeNode::getId,
                                        Function.identity()
                                )
                        );

        for (DocumentProcessing processing : processings) {
            mapDocument(
                    processing,
                    candidates,
                    scopeNodeMap
            );
        }

        log.info(
                "처리 그룹 문서 목차 매핑 완료. processingGroupId={}, documentCount={}, scopeNodeCount={}",
                processingGroupId,
                processings.size(),
                scopeNodes.size()
        );
    }


    /**
     * 문서 한 개에 속한 모든 청크를 목차에 매핑한다.
     */
    private void mapDocument(
            DocumentProcessing processing,
            List<ExamScopeCandidateDto> candidates,
            Map<Long, ExamScopeNode> scopeNodeMap
    ) {
        Long sourceDocumentId =
                processing.getSourceDocument().getId();

        List<DocumentChunk> chunks =
                documentChunkRepository
                        .findAllBySourceDocument_IdOrderByChunkOrderAsc(
                                sourceDocumentId
                        );

        if (chunks.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }

        documentScopeMappingRepository
                .deleteAllByDocumentChunk_SourceDocument_Id(
                        sourceDocumentId
                );

        for (DocumentChunk chunk : chunks) {
            mapChunk(
                    chunk,
                    candidates,
                    scopeNodeMap
            );
        }

        long mappingCount =
                documentScopeMappingRepository
                        .countByDocumentChunk_SourceDocument_Id(
                                sourceDocumentId
                        );

        if (mappingCount != chunks.size()) {
            log.error(
                    "문서 청크 매핑 개수가 일치하지 않습니다. sourceDocumentId={}, chunkCount={}, mappingCount={}",
                    sourceDocumentId,
                    chunks.size(),
                    mappingCount
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        log.info(
                "문서 목차 매핑 완료. processingId={}, sourceDocumentId={}, chunkCount={}, mappingCount={}",
                processing.getId(),
                sourceDocumentId,
                chunks.size(),
                mappingCount
        );
    }
    /**
     * 청크 하나를 적절한 ExamScopeNode에 매핑한다.
     *
     * 현재는 AI 매핑 로직을 연결하기 전 단계다.
     */
    private void mapChunk(
            DocumentChunk chunk,
            List<ExamScopeCandidateDto> candidates,
            Map<Long, ExamScopeNode> scopeNodeMap
    ) {
        String chunkContent =
                chunk.getContentText();

        if (chunkContent == null
                || chunkContent.isBlank()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }

        DocumentScopeMappingResultDto result =
                documentScopeMappingClient.mapChunk(
                        chunkContent,
                        candidates
                );

        ExamScopeNode selectedScopeNode =
                scopeNodeMap.get(
                        result.examScopeNodeId()
                );

        if (selectedScopeNode == null) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        DocumentScopeMapping mapping =
                DocumentScopeMapping.builder()
                        .documentChunk(chunk)
                        .examScopeNode(selectedScopeNode)
                        .mappingReason(
                                result.mappingReason()
                        )
                        .build();

        documentScopeMappingRepository.save(mapping);

        log.info(
                "청크 목차 매핑 저장 완료. chunkId={}, chunkOrder={}, examScopeNodeId={}",
                chunk.getId(),
                chunk.getChunkOrder(),
                selectedScopeNode.getId()
        );
    }

    private List<DocumentProcessing> getDocumentProcessings(
            Long processingGroupId
    ) {
        List<DocumentProcessing> processings =
                documentProcessingRepository
                        .findAllByProcessingGroup_IdOrderByIdAsc(
                                processingGroupId
                        );

        if (processings.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_PROCESSING_NOT_FOUND
            );
        }

        return processings;
    }

    private List<ExamScopeNode> getExamScopeNodes(
            Long processingGroupId
    ) {
        DocumentProcessingGroup processingGroup =
                documentProcessingGroupRepository
                        .findById(processingGroupId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND
                                )
                        );

        Long examVersionId =
                processingGroup
                        .getUserExam()
                        .getExamVersion()
                        .getId();

        List<ExamScopeNode> scopeNodes =
                examScopeNodeRepository
                        .findActiveNodesByExamVersionIdAndNodeTypes(
                                examVersionId,
                                List.of(
                                        ExamScopeNodeType.TOPIC
                                )
                        );

        if (scopeNodes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND
            );
        }

        log.info(
                "시험 목차 후보 조회 완료. processingGroupId={}, examVersionId={}, scopeNodeCount={}",
                processingGroupId,
                examVersionId,
                scopeNodes.size()
        );

        return scopeNodes;
    }

    private List<ExamScopeCandidateDto> createScopeCandidates(
            List<ExamScopeNode> scopeNodes
    ) {
        return scopeNodes.stream()
                .map(scopeNode ->
                        new ExamScopeCandidateDto(
                                scopeNode.getId(),
                                scopeNode.getTitle(),
                                createScopePath(scopeNode)
                        )
                )
                .toList();
    }

    private String createScopePath(
            ExamScopeNode scopeNode
    ) {
        List<String> titles = new ArrayList<>();

        ExamScopeNode currentNode = scopeNode;

        while (currentNode != null) {
            titles.add(currentNode.getTitle());
            currentNode = currentNode.getParent();
        }

        Collections.reverse(titles);

        return String.join(" > ", titles);
    }
}