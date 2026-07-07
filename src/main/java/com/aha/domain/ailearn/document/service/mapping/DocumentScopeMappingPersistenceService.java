package com.aha.domain.ailearn.document.service.mapping;

import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentScopeMappingPersistenceService
 * @since : 2026. 6. 24. 수요일
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentScopeMappingPersistenceService {

    private final DocumentScopeMappingRepository documentScopeMappingRepository;

    @Transactional
    public void replaceMappings(Long processingGroupId, List<DocumentScopeMapping> mappings){

        validateInput(processingGroupId, mappings);

        int deletedCount = documentScopeMappingRepository.deleteAllByProcessingGroupId(processingGroupId);

        if(mappings.isEmpty()){

            log.info(
                    "문서 목차 매핑 결과 없음. processingGroupId={}, deletedCount={}",
                    processingGroupId,
                    deletedCount
            );

            return;
        }

        documentScopeMappingRepository.saveAll(mappings);

        log.info(
                "문서 목차 매핑 교체 완료. processingGroupId={}, mappingCount={}",
                processingGroupId,
                mappings.size()
        );

    }

    private void validateInput(Long processingGroupId, List<DocumentScopeMapping> mappings) {

        if(processingGroupId == null){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if(mappings == null || mappings.isEmpty()){
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }
    }

}
