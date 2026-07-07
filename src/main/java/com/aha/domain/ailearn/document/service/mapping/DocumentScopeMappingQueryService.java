package com.aha.domain.ailearn.document.service.mapping;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentScopeMappingQueryService
 * @since : 2026. 6. 25. 목요일
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentScopeMappingQueryService {

    private final DocumentScopeMappingRepository documentScopeMappingRepository;

    public List<DocumentChunk> getMappedChunks(Long userExamId, Long examScopeNodeId) {

        validateInput(userExamId, examScopeNodeId);

        List<DocumentScopeMapping> mappings = documentScopeMappingRepository.findAllByUserExamIdAndExamScopeNodeId(userExamId, examScopeNodeId);

        return mappings.stream()
                .map(DocumentScopeMapping::getDocumentChunk)
                .distinct()
                .toList();
    }

    private void validateInput(Long userExamId, Long examScopeNodeId) {

        if (userExamId == null || examScopeNodeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }


}
