package com.aha.domain.document.service.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingWorkerService {

    private final DocumentProcessingStatusService processingStatusService;
    private final DocumentProcessingContextService processingContextService;
    private final DocumentProcessingCoordinator processingCoordinator;

    public void process(Long processingId) {

        try{
            processingStatusService.start(processingId);

            var context = processingContextService.get(processingId);

            log.info(
                    "문서 처리 시작. processingId={}, learningNoteId={}",
                    processingId,
                    context.learningNoteId()
            );

            processingCoordinator.process(context);

            processingStatusService.complete(processingId);

            log.info(
                    "문서 처리 완료. processingId={}, learningNoteId={}",
                    processingId,
                    context.learningNoteId()
            );
        }catch (Exception exception){
            log.error(
                    "문서 처리 실패. processingId={}",
                    processingId,
                    exception
            );

            markProcessingFailedSafely(
                    processingId,
                    exception
            );
        }
    }

    private void markProcessingFailedSafely(Long processingId, Exception exception) {

        try{
            processingStatusService.fail(
                    processingId,
                    exception
            );
        }catch (Exception statusException){
            log.error(
                    "문서 처리 실패 상태 저장에 실패했습니다. processingId={}",
                    processingId,
                    statusException
            );
        }
    }
}

