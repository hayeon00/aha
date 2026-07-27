package com.aha.domain.notestudio.controller;

import com.aha.domain.notestudio.document.dto.upload.response.BatchUploadResponseDto;
import com.aha.domain.notestudio.document.dto.content.response.DocumentConceptDashboardResponseDto;
import com.aha.domain.notestudio.document.dto.content.response.DocumentProcessingGroupResponseDto;
import com.aha.domain.notestudio.document.dto.content.response.DocumentProcessingStateResponseDto;
import com.aha.domain.notestudio.document.dto.content.response.OwnedDocumentResponseDto;
import com.aha.domain.notestudio.document.service.DocumentService;
import com.aha.domain.notestudio.document.service.content.UserDocumentConceptService;
import com.aha.domain.notestudio.document.service.processing.DocumentProcessingQueryService;
import com.aha.domain.notestudio.document.service.processing.DocumentProcessingRetryService;
import com.aha.domain.notestudio.document.service.upload.DocumentUploadService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentUploadService documentUploadService;
    private final DocumentService documentService;
    private final DocumentProcessingQueryService processingQueryService;
    private final DocumentProcessingRetryService processingRetryService;
    private final UserDocumentConceptService conceptService;

    @PostMapping("/api/v1/document/upload")
    public ResponseEntity<ApiResponse<BatchUploadResponseDto>> uploadDocuments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("userExamId") Long userExamId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        BatchUploadResponseDto response =
                documentUploadService.uploadDocumentsBatch(
                        userDetails.getId(),
                        userExamId,
                        files
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        ApiResponse.success(
                                202,
                                "학습 문서 업로드 성공",
                                response
                        )
                );
    }

    @DeleteMapping("/api/v1/document/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long documentId
    ){
        documentService.delete(user.getId(), documentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "문서 삭제 성공",
                        null
                )
        );
    }

    @GetMapping("/api/documents/user-exams/{userExamId}")
    public ResponseEntity<ApiResponse<List<OwnedDocumentResponseDto>>> getDocuments(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long userExamId) {
        return ok("업로드한 문서 목록을 조회했습니다.",
                conceptService.getOwnedDocuments(user.getId(), userExamId));
    }

    @GetMapping("/api/documents/{documentId}/concepts")
    public ResponseEntity<ApiResponse<DocumentConceptDashboardResponseDto>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long documentId) {
        return ok("문서별 개념 현황을 조회했습니다.",
                conceptService.getDashboard(user.getId(), documentId));
    }

    @PostMapping("/api/v1/ai-learning/document-processings/{processingGroupId}/retry")
    public ResponseEntity<ApiResponse<DocumentProcessingGroupResponseDto>> retryProcessing(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long processingGroupId) {
        return ResponseEntity.accepted().body(ApiResponse.success(
                HttpStatus.ACCEPTED.value(),
                "문서 분석 재시도가 접수되었습니다.",
                processingRetryService.retry(user.getId(), processingGroupId)));
    }

    @GetMapping("/api/v1/ai-learning/document-processings/{processingGroupId}")
    public ResponseEntity<ApiResponse<DocumentProcessingGroupResponseDto>> getProcessingGroup(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long processingGroupId) {
        return ok("문서 처리 상태 조회에 성공했습니다.",
                processingQueryService.getProcessingGroup(user.getId(), processingGroupId));
    }

    @GetMapping("/api/v1/ai-learning/document-processings/user-exams/{userExamId}/latest")
    public ResponseEntity<ApiResponse<DocumentProcessingStateResponseDto>> getLatestProcessingState(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long userExamId) {
        return ok("최근 문서 처리 상태 조회에 성공했습니다.",
                processingQueryService.getLatestProcessingState(user.getId(), userExamId));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, data));
    }
}
