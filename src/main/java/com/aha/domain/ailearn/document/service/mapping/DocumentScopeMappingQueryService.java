package com.aha.domain.ailearn.document.service.mapping;

import com.aha.domain.ailearn.document.dto.content.response.MappedDocumentChunkResponseDto;
import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentScopeMappingQueryService {

    private final DocumentScopeMappingRepository documentScopeMappingRepository;

    public List<MappedDocumentChunkResponseDto> getMappedDocumentChunks(Long id, Long userExamId, Long examScopeNodeId) {
        if (id == null || userExamId == null || examScopeNodeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return documentScopeMappingRepository
                .findMappedChunks(id, userExamId, examScopeNodeId)
                .stream()
                .map(MappedDocumentChunkResponseDto::from)
                .toList();
    }
}
