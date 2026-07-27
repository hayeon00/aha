package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.content.response.DocumentProcessingGroupResponseDto;
import com.aha.domain.ailearn.document.dto.content.response.DocumentProcessingStateResponseDto;
import com.aha.domain.ailearn.document.service.processing.DocumentProcessingQueryService;
import com.aha.domain.ailearn.document.service.processing.DocumentProcessingRetryService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-learning/document-processings")
@RequiredArgsConstructor
public class DocumentProcessingController {

    private final DocumentProcessingQueryService documentProcessingQueryService;
    private final DocumentProcessingRetryService documentProcessingRetryService;


    // 문서 업로드에 실패했을 경우 프론트에서 재시도 요청
    @PostMapping("/{processingGroupId}/retry")
    public ResponseEntity<ApiResponse<DocumentProcessingGroupResponseDto>> retryProcessing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long processingGroupId
    ) {

        DocumentProcessingGroupResponseDto response =
                documentProcessingRetryService.retry(userDetails.getId(), processingGroupId);

        return ResponseEntity.accepted().body(ApiResponse.success(
                HttpStatus.ACCEPTED.value(),
                "문서 분석 재시도가 접수되었습니다.",
                response
        ));
    }


    // 처리 상태 조회 -> 프론트에서 2초마다 폴링
    @GetMapping("/{processingGroupId}")
    public ResponseEntity<ApiResponse<DocumentProcessingGroupResponseDto>> getProcessingGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long processingGroupId
    ) {
        DocumentProcessingGroupResponseDto response =
                documentProcessingQueryService.getProcessingGroup(
                                userDetails.getId(),
                                processingGroupId
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "문서 처리 상태 조회에 성공했습니다.",
                        response
                )
        );
    }

    // 가장 최근 처리 상태 조회, 새로고침 이후에도 기존 작업 복구
    @GetMapping("/user-exams/{userExamId}/latest")
    public ResponseEntity<ApiResponse<DocumentProcessingStateResponseDto>> getLatestProcessingState(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId
    ) {
        DocumentProcessingStateResponseDto response =
                documentProcessingQueryService
                        .getLatestProcessingState(
                                userDetails.getId(),
                                userExamId
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "최근 문서 처리 상태 조회에 성공했습니다.",
                        response
                )
        );
    }
}
