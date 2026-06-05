package com.aha.domain.ailearn.document.service;

import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import com.aha.domain.ailearn.document.enums.SourceDocumentStatus;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.DocumentProcessingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentProcessingStatusService {

    private final DocumentProcessingGroupRepository documentProcessingGroupRepository;
    private final DocumentProcessingRepository documentProcessingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void start(Long processingGroupId) {
        DocumentProcessingGroup processingGroup = getProcessingGroup(processingGroupId);
        processingGroup.startProcessing();

        List<DocumentProcessing> processings =
                documentProcessingRepository.findAllByProcessingGroup_IdOrderByIdAsc(processingGroupId);

        processings.forEach(processing -> {
            processing.startProcessing();
            processing.getSourceDocument().updateStatus(SourceDocumentStatus.ANALYZING);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStep(Long processingGroupId, DocumentProcessingStep step) {
        DocumentProcessingGroup processingGroup = getProcessingGroup(processingGroupId);
        processingGroup.updateStep(step);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long processingGroupId) {
        DocumentProcessingGroup processingGroup = getProcessingGroup(processingGroupId);
        List<DocumentProcessing> processings =
                documentProcessingRepository.findAllByProcessingGroup_IdOrderByIdAsc(processingGroupId);

        processings.forEach(processing -> {
            processing.complete();
            processing.getSourceDocument().updateStatus(SourceDocumentStatus.READY);
            processingGroup.increaseCompletedFileCount();
        });

        processingGroup.complete();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long processingGroupId, String errorMessage) {
        DocumentProcessingGroup processingGroup = getProcessingGroup(processingGroupId);
        List<DocumentProcessing> processings =
                documentProcessingRepository.findAllByProcessingGroup_IdOrderByIdAsc(processingGroupId);

        processings.forEach(processing -> {
            processing.fail(errorMessage);
            processing.getSourceDocument().updateStatus(SourceDocumentStatus.FAILED);
            processingGroup.increaseFailedFileCount();
        });

        processingGroup.fail(errorMessage);
    }

    private DocumentProcessingGroup getProcessingGroup(Long processingGroupId) {
        return documentProcessingGroupRepository.findById(processingGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));
    }
}
