package com.aha.domain.ailearn.document.service.content;

import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.ailearn.document.repository.projection.MappedTopicChunkProjection;
import com.aha.domain.ailearn.document.service.content.model.TopicDocumentContext;
import com.aha.domain.ailearn.document.service.content.model.TopicDocumentChunk;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentContentContextQueryService {

    private final DocumentScopeMappingRepository
            documentScopeMappingRepository;

    /**
     * 처리 그룹의 문서 매핑 결과를 Topic별로 묶어 반환한다.
     */
    @Transactional(readOnly = true)
    public List<TopicDocumentContext> findTopicContexts(
            Long processingGroupId
    ) {
        validateProcessingGroupId(processingGroupId);

        List<MappedTopicChunkProjection> mappings =
                documentScopeMappingRepository
                        .findMappedTopicChunks(
                                processingGroupId
                        );

        if (mappings.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_NOT_FOUND
            );
        }

        Map<Long, TopicContextAccumulator> groupedContexts =
                new LinkedHashMap<>();

        for (MappedTopicChunkProjection mapping : mappings) {
            TopicContextAccumulator accumulator =
                    groupedContexts.computeIfAbsent(
                            mapping.getTopicId(),
                            topicId ->
                                    new TopicContextAccumulator(
                                            topicId,
                                            mapping.getTopicTitle()
                                    )
                    );

            accumulator.addChunk(
                    new TopicDocumentChunk(
                            mapping.getChunkId(),
                            mapping.getChunkOrder(),
                            mapping.getChunkType(),
                            mapping.getChunkText()
                    )
            );
        }

        return groupedContexts.values()
                .stream()
                .map(TopicContextAccumulator::toContext)
                .toList();
    }

    private void validateProcessingGroupId(
            Long processingGroupId
    ) {
        if (processingGroupId == null
                || processingGroupId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private static class TopicContextAccumulator {

        private final Long topicId;
        private final String topicTitle;
        private final List<TopicDocumentChunk> chunks =
                new java.util.ArrayList<>();

        private TopicContextAccumulator(
                Long topicId,
                String topicTitle
        ) {
            this.topicId = topicId;
            this.topicTitle = topicTitle;
        }

        private void addChunk(TopicDocumentChunk chunk) {
            if (chunk == null) {
                return;
            }
            chunks.add(chunk);
        }

        private TopicDocumentContext toContext() {
            return new TopicDocumentContext(
                    topicId,
                    topicTitle,
                    chunks
            );
        }
    }
}
