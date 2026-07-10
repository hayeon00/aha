package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.content.response.MappedDocumentChunkResponseDto;
import com.aha.domain.ailearn.document.service.mapping.DocumentScopeMappingQueryService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-learning/document-contents")
@RequiredArgsConstructor
public class DocumentLearningContentController {

    private final DocumentScopeMappingQueryService documentScopeMappingQueryService;

    @GetMapping("/user-exams/{userExamId}/scope-nodes/{examScopeNodeId}/mapped-chunks")
    public ResponseEntity<ApiResponse<List<MappedDocumentChunkResponseDto>>> getMappedDocumentChunks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId,
            @PathVariable Long examScopeNodeId
    ) {
        List<MappedDocumentChunkResponseDto> response =
                documentScopeMappingQueryService.getMappedDocumentChunks(
                        userExamId,
                        examScopeNodeId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "선택한 목차에 매핑된 문서 내용을 조회했습니다.",
                        response
                )
        );
    }
}