package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.mapping.request.ManualScopeMappingRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.response.UnassignedDocumentChunkResponseDto;
import com.aha.domain.ailearn.document.service.mapping.ManualDocumentScopeMappingService;
import com.aha.domain.ailearn.document.service.mapping.DocumentScopeMappingQueryService;
import com.aha.domain.ailearn.document.dto.content.response.MappedDocumentChunkResponseDto;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-learning/document-mappings")
@RequiredArgsConstructor
public class DocumentScopeMappingController {

    private final ManualDocumentScopeMappingService manualMappingService;
    private final DocumentScopeMappingQueryService mappingQueryService;

    @GetMapping("/user-exams/{userExamId}/scope-nodes/{examScopeNodeId}/chunks")
    public ResponseEntity<ApiResponse<List<MappedDocumentChunkResponseDto>>> getMappedChunks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId,
            @PathVariable Long examScopeNodeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "목차에 매핑된 문서 청크 조회에 성공했습니다.",
                mappingQueryService.getMappedDocumentChunks(
                        userDetails.getId(), userExamId, examScopeNodeId
                )
        ));
    }

    @GetMapping("/user-exams/{userExamId}/unassigned")
    public ResponseEntity<ApiResponse<List<UnassignedDocumentChunkResponseDto>>> getUnassigned(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "미분류 문서 청크 조회에 성공했습니다.",
                manualMappingService.getUnassignedChunks(userDetails.getId(), userExamId)
        ));
    }

    @PutMapping("/chunks/{chunkId}")
    public ResponseEntity<ApiResponse<Void>> assign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chunkId,
            @RequestBody ManualScopeMappingRequestDto request
    ) {
        manualMappingService.assign(userDetails.getId(), chunkId, request.examScopeNodeId());
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "문서 청크의 목차를 지정했습니다.",
                null
        ));
    }
}
