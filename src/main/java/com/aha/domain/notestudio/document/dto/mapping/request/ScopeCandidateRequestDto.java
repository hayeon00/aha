package com.aha.domain.notestudio.document.dto.mapping.request;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ExamScopeNodeMappingRequest
 * @since : 2026. 6. 24. 수요일
 */
public record ScopeCandidateRequestDto(
        Long examScopeNodeId,
        String code,
        String title,
        String description,
        String keywordsJson,
        Double similarityScore
){

}
