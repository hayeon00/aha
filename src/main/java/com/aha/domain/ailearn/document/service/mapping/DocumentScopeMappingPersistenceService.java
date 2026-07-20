package com.aha.domain.ailearn.document.service.mapping;

import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
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
public class DocumentScopeMappingPersistenceService {

    private final DocumentScopeMappingRepository documentScopeMappingRepository;

    @Transactional
    public void replaceMappings(Long processingGroupId, List<DocumentScopeMapping> mappings) {
        validateProcessingGroupId(processingGroupId);

        int deletedCount = documentScopeMappingRepository.deleteAllByProcessingGroupId(processingGroupId);

        if (mappings == null || mappings.isEmpty()) {
            log.info(
                    "문서 목차 매핑 결과 없음. processingGroupId={}, deletedCount={}",
                    processingGroupId,
                    deletedCount
            );
            return;
        }

        documentScopeMappingRepository.saveAll(mappings);

        log.info(
                "문서 목차 매핑 교체 완료. processingGroupId={}, deletedCount={}, mappingCount={}",
                processingGroupId,
                deletedCount,
                mappings.size()
        );
    }

    private void validateProcessingGroupId(Long processingGroupId) {
        if (processingGroupId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}