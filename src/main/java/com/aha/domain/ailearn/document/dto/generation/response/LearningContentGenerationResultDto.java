package com.aha.domain.ailearn.document.dto.generation.response;

import java.util.List;

public record LearningContentGenerationResultDto(
        String title,
        String content,
        List<String> keywords
) {
}