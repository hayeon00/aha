package com.aha.domain.document.controller;

import com.aha.domain.document.dto.response.DocumentProcessingResponseDto;
import com.aha.domain.document.service.processing.DocumentProcessingQueryService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/document-processings")
public class DocumentProcessingController {

    private final DocumentProcessingQueryService processingQueryService;

    @GetMapping("/{processingId}")
    public ResponseEntity<ApiResponse<DocumentProcessingResponseDto>> getProcessing(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long processingId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "문서 처리 상태 조회 성공",
                        processingQueryService.get(user.getId(), processingId)
                )
        );
    }
}
