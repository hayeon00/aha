package com.aha.domain.notestudio.document.event;

public record DocumentUploadCompletedEvent(
        Long processingGroupId
) {
}