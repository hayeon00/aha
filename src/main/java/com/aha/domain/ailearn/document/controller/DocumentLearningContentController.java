package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.content.response.DocumentBasedLearningContentResponseDto;
import com.aha.domain.ailearn.document.service.content.DocumentBasedLearningContentGenerationService;
import com.aha.domain.ailearn.document.service.content.DocumentLearningContentBatchGenerationService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentLearningContentController
 * @since : 2026. 6. 25. 목요일
 */

@RestController
@RequestMapping("/api/v1/ai-learning/document-contents")
@RequiredArgsConstructor
public class DocumentLearningContentController {

    private final DocumentBasedLearningContentGenerationService generationService;
    private final DocumentLearningContentBatchGenerationService batchGenerationService;

    @PostMapping("/user-exams/{userExamId}/scope-nodes/{examScopeNodeId}/generate")
    public ResponseEntity<ApiResponse<DocumentBasedLearningContentResponseDto>>
    generateLearningContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId,
            @PathVariable Long examScopeNodeId
    ) {
        DocumentBasedLearningContentResponseDto response =
                generationService.generate(
                        userDetails.getId(),
                        userExamId,
                        examScopeNodeId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "선택한 목차의 개념 설명 생성 및 저장이 완료되었습니다.",
                        response
                )
        );
    }

}
