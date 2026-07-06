package com.aha.domain.ailearn.document.service.processing;

import com.aha.domain.ailearn.document.config.DocumentProcessingProperties;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentProcessingRetryExecutor
 * @since : 2026. 7. 6. 월요일
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentProcessingRetryExecutor {

    private final DocumentProcessingProperties properties;

    public <T> T execute(String taskName, Supplier<T> task) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= properties.getRetryMaxAttempts(); attempt++) {
            try {
                return executeWithTimeout(taskName, task);
            } catch (RuntimeException exception) {
                lastException = exception;

                if (!isRetryable(exception) || attempt == properties.getRetryMaxAttempts()) {
                    break;
                }

                long backoffMillis = calculateBackoffMillis(attempt);

                log.warn(
                        "문서 처리 작업 재시도 예정. taskName={}, attempt={}, backoffMillis={}",
                        taskName,
                        attempt,
                        backoffMillis,
                        exception
                );

                sleep(backoffMillis);
            }
        }

        log.error(
                "문서 처리 작업 최종 실패. taskName={}, maxAttempts={}",
                taskName,
                properties.getRetryMaxAttempts(),
                lastException
        );

        throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
    }

    private <T> T executeWithTimeout(String taskName, Supplier<T> task) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            Future<T> future = executorService.submit(task::get);

            return future.get(
                    properties.getTimeoutSeconds(),
                    TimeUnit.SECONDS
            );

        } catch (TimeoutException exception) {
            log.error(
                    "문서 처리 작업 timeout. taskName={}, timeoutSeconds={}",
                    taskName,
                    properties.getTimeoutSeconds(),
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);

        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();

            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);

        } finally {
            executorService.shutdownNow();
        }
    }

    private boolean isRetryable(RuntimeException exception) {

        return !(exception instanceof BusinessException);
    }

    private long calculateBackoffMillis(int attempt) {
        return properties.getRetryBackoffMillis() * (long) Math.pow(2, attempt - 1);
    }

    private void sleep(long backoffMillis) {
        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.DOCUMENT_UPLOAD_FAILED);
        }
    }
}