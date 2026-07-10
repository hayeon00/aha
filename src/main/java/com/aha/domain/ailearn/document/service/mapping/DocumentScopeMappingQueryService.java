package com.aha.domain.ailearn.document.service.mapping;

import com.aha.domain.ailearn.document.dto.content.response.MappedDocumentChunkResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentScopeMappingQueryService {

    private final DocumentScopeMappingRepository documentScopeMappingRepository;

    public List<DocumentChunk> getMappedChunks(Long userExamId, Long examScopeNodeId) {
        List<DocumentScopeMapping> mappings = getMappings(userExamId, examScopeNodeId);

        return mappings.stream()
                .map(DocumentScopeMapping::getDocumentChunk)
                .filter(Objects::nonNull)
                .collect(
                        LinkedHashMap<Long, DocumentChunk>::new,
                        (map, chunk) -> map.putIfAbsent(chunk.getId(), chunk),
                        Map::putAll
                )
                .values()
                .stream()
                .sorted(Comparator.comparing(
                        DocumentChunk::getChunkOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .toList();
    }

    public List<MappedDocumentChunkResponseDto> getMappedDocumentChunks(
            Long userExamId,
            Long examScopeNodeId
    ) {
        return getMappedChunks(userExamId, examScopeNodeId).stream()
                .map(MappedDocumentChunkResponseDto::from)
                .toList();
    }

    private List<DocumentScopeMapping> getMappings(Long userExamId, Long examScopeNodeId) {
        validateInput(userExamId, examScopeNodeId);

        List<DocumentScopeMapping> mappings =
                documentScopeMappingRepository.findAllByUserExamIdAndExamScopeNodeId(
                        userExamId,
                        examScopeNodeId
                );

        if (mappings.isEmpty()) {
            log.info(
                    "목차에 매핑된 문서 청크가 없습니다. userExamId={}, examScopeNodeId={}",
                    userExamId,
                    examScopeNodeId
            );
        }

        return mappings;
    }

    private void validateInput(Long userExamId, Long examScopeNodeId) {
        if (userExamId == null || examScopeNodeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}