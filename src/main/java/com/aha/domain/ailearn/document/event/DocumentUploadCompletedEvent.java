package com.aha.domain.ailearn.document.event;

public record DocumentUploadCompletedEvent(
        Long processingGroupId
) {
}