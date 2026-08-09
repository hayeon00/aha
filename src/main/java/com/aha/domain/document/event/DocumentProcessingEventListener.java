package com.aha.domain.document.event;

import com.aha.domain.document.service.processing.DocumentProcessingWorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentProcessingEventListener {

    private final DocumentProcessingWorkerService workerService;

    @Async("documentProcessingTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DocumentProcessingRequestedEvent event) {
        log.info(
                "문서 처리 요청 이벤트 수신. processingId={}",
                event.processingId()
        );

        workerService.process(event.processingId());
    }
}
