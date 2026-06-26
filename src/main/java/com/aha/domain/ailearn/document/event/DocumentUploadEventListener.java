package com.aha.domain.ailearn.document.event;

import com.aha.domain.ailearn.document.service.processing.DocumentProcessingWorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DocumentUploadEventListener {

    private final DocumentProcessingWorkerService documentProcessingWorkerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DocumentUploadCompletedEvent event) {

        documentProcessingWorkerService.process(
                event.processingGroupId()
        );
    }
}