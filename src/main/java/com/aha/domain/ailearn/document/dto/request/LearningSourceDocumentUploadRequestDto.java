package com.aha.domain.ailearn.document.dto.request;

import com.aha.domain.ailearn.document.type.SourceType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LearningSourceDocumentUploadRequestDto {

    private Long examId;
    private Long examPartId;
    private Long examScopeNodeId;
    private String title;
    private SourceType sourceType;
    private String description;
}
