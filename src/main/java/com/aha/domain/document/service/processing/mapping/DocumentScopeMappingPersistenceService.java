package com.aha.domain.document.service.processing.mapping;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.DocumentScopeMapping;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.document.repository.DocumentScopeMappingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentScopeMappingPersistenceService {

    private final DocumentScopeMappingRepository documentScopeMappingRepository;
    private final DocumentChunkRepository documentChunkRepository;

    /**
     * 외부 AI 호출이 끝난 뒤 최종 결과만 하나의 transaction으로 반영한다.
     *
     * 1. 기존 mapping 삭제
     * 2. 새 mapping 저장
     * 3. chunk mapping status 갱신
     *
     * 세 작업을 원자적으로 처리하여 mapping과 chunk status가
     * 서로 다른 상태로 남는 것을 방지한다.
     */
    @Transactional
    public void replaceMappingsAndUpdateChunkStatuses(
            Long learningNoteId,
            List<DocumentChunk> chunks,
            List<DocumentScopeMapping> mappings
    ) {
        validateLearningNoteId(
                learningNoteId
        );

        List<DocumentChunk> safeChunks =
                chunks == null
                        ? List.of()
                        : chunks;

        List<DocumentScopeMapping> safeMappings =
                mappings == null
                        ? List.of()
                        : mappings;

        int deletedCount =
                documentScopeMappingRepository
                        .deleteAllByLearningNoteId(
                                learningNoteId
                        );

        if (!safeMappings.isEmpty()) {
            documentScopeMappingRepository
                    .saveAll(
                            safeMappings
                    );
        }

        Set<Long> mappedChunkIds =
                safeMappings.stream()
                        .map(DocumentScopeMapping::getDocumentChunk)
                        .filter(chunk ->
                                chunk != null
                                        && chunk.getId() != null
                        )
                        .map(DocumentChunk::getId)
                        .collect(
                                Collectors.toSet()
                        );

        for (DocumentChunk chunk : safeChunks) {

            if (chunk == null
                    || chunk.getId() == null) {
                continue;
            }

            if (mappedChunkIds.contains(
                    chunk.getId()
            )) {
                chunk.markMapped();
            } else {
                chunk.markUnassigned();
            }
        }

        if (!safeChunks.isEmpty()) {
            documentChunkRepository.saveAll(
                    safeChunks
            );
        }

        log.info(
                "문서 목차 매핑 결과 반영 완료. learningNoteId={}, deletedCount={}, mappingCount={}, chunkCount={}",
                learningNoteId,
                deletedCount,
                safeMappings.size(),
                safeChunks.size()
        );
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
}