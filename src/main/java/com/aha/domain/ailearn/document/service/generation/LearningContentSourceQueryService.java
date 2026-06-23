package com.aha.domain.ailearn.document.service.generation;

import com.aha.domain.ailearn.document.dto.generation.request.TopicLearningContentSourceDto;
import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningContentSourceQueryService {

    private final DocumentScopeMappingRepository
            documentScopeMappingRepository;

    /**
     * 트랜잭션 안에서 엔티티와 LAZY 연관관계를 모두 읽고
     * AI 호출에 사용할 DTO로 변환한다.
     */
    @Transactional(readOnly = true)
    public List<TopicLearningContentSourceDto> findSources(
            Long processingGroupId
    ) {
        List<DocumentScopeMapping> mappings =
                documentScopeMappingRepository
                        .findAllByProcessingGroupId(
                                processingGroupId
                        );

        if (mappings.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_NOT_FOUND
            );
        }

        Map<Long, ScopeChunkGroup> groupedMappings =
                new LinkedHashMap<>();

        for (DocumentScopeMapping mapping : mappings) {
            ExamScopeNode scopeNode =
                    mapping.getExamScopeNode();

            DocumentChunk documentChunk =
                    mapping.getDocumentChunk();

            String chunkContent =
                    documentChunk.getContentText();

            if (chunkContent == null
                    || chunkContent.isBlank()) {
                continue;
            }

            ScopeChunkGroup group =
                    groupedMappings.computeIfAbsent(
                            scopeNode.getId(),
                            ignored ->
                                    new ScopeChunkGroup(
                                            scopeNode,
                                            new ArrayList<>()
                                    )
                    );

            group.chunkContents().add(
                    chunkContent.trim()
            );
        }

        List<TopicLearningContentSourceDto> sources =
                groupedMappings.values()
                        .stream()
                        .filter(group ->
                                !group.chunkContents().isEmpty()
                        )
                        .map(group ->
                                new TopicLearningContentSourceDto(
                                        group.scopeNode().getId(),
                                        group.scopeNode().getTitle(),
                                        createScopePath(
                                                group.scopeNode()
                                        ),
                                        List.copyOf(
                                                group.chunkContents()
                                        )
                                )
                        )
                        .toList();

        if (sources.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
        }

        return sources;
    }

    private String createScopePath(
            ExamScopeNode scopeNode
    ) {
        List<String> titles =
                new ArrayList<>();

        ExamScopeNode currentNode =
                scopeNode;

        while (currentNode != null) {
            titles.add(
                    0,
                    currentNode.getTitle()
            );

            currentNode =
                    currentNode.getParent();
        }

        return String.join(
                " > ",
                titles
        );
    }

    private record ScopeChunkGroup(
            ExamScopeNode scopeNode,
            List<String> chunkContents
    ) {
    }
}