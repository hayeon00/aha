package com.aha.domain.document.service.processing.mapping;

import com.aha.domain.document.entity.DocumentScopeMapping;
import com.aha.domain.document.repository.DocumentScopeMappingRepository;
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
    public void replaceMappings(Long learningNoteId, List<DocumentScopeMapping> mappings) {
        validateLearningNoteId(learningNoteId);

        int deletedCount = documentScopeMappingRepository.deleteAllByLearningNoteId(learningNoteId);

        if (mappings == null || mappings.isEmpty()) {
            log.info(
                    "문서 목차 매핑 결과 없음. learningNoteId={}, deletedCount={}",
                    learningNoteId,
                    deletedCount
            );
            return;
        }

        documentScopeMappingRepository.saveAll(mappings);

        log.info(
                "문서 목차 매핑 교체 완료. learningNoteId={}, deletedCount={}, mappingCount={}",
                learningNoteId,
                deletedCount,
                mappings.size()
        );
    }

    private void validateLearningNoteId(Long learningNoteId) {
        if (learningNoteId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
