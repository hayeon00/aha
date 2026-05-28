package com.aha.domain.ailearn.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GeneratedLearningContentBodyListResponseDto {
    private Long sourceDocumentId;
    private List<GeneratedLearningContentBodyResponseDto> bodies;
}
