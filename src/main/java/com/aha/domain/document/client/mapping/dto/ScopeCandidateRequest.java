package com.aha.domain.document.client.mapping.dto;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ExamScopeNodeMappingRequest
 * @since : 2026. 6. 24. 수요일
 */
public record ScopeCandidateRequest(
        Long examScopeNodeId,
        String code,
        String title,
        String description,
        String keywordsJson,
        Double similarityScore
){

}
