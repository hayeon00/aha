package com.aha.domain.ailearn.document.event;

import com.aha.domain.ailearn.document.service.processing.DocumentProcessingStatusService;
import com.aha.domain.ailearn.document.service.processing.DocumentProcessingWorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadEventListener {

    private final DocumentProcessingWorkerService documentProcessingWorkerService;
    private final DocumentProcessingStatusService documentProcessingStatusService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DocumentUploadCompletedEvent event) {

        try{
            documentProcessingWorkerService.process(event.processingGroupId());

        }catch(TaskRejectedException exception){
            log.error(
                    "문서 처리 작업 등록 실패. executor queue is full. processingGroupId={}",
                    event.processingGroupId(),
                    exception
            );

            documentProcessingStatusService.fail(
                    event.processingGroupId(),
                    "문서 처리 요청이 많아 작업을 시작하지 못했습니다. 잠시 후 다시 시도해주세요."
            );
        }
    }
}