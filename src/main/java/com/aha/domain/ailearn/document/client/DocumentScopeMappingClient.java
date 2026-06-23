package com.aha.domain.ailearn.document.client;

import com.aha.domain.ailearn.document.dto.mapping.request.ExamScopeCandidateDto;
import com.aha.domain.ailearn.document.dto.mapping.response.DocumentScopeMappingResultDto;

import java.util.List;

public interface DocumentScopeMappingClient {

    DocumentScopeMappingResultDto mapChunk(
            String chunkContent,
            List<ExamScopeCandidateDto> candidates
    );
}