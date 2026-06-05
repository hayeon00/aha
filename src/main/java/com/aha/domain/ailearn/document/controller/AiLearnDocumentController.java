package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.response.DocumentBatchUploadResponseDto;
import com.aha.domain.ailearn.document.dto.response.DocumentProcessingGroupResponseDto;
import com.aha.domain.ailearn.document.service.AiLearnDocumentService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aiLearn/documents")
@RequiredArgsConstructor
public class AiLearnDocumentController {

    private final AiLearnDocumentService ailearnDocumentService;

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<DocumentBatchUploadResponseDto>> uploadDocumentsBatch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("userExamId") Long userExamId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        DocumentBatchUploadResponseDto response =
                ailearnDocumentService.uploadDocumentsBatch(
                        userDetails.getId(),
                        userExamId,
                        files
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "학습 문서 업로드에 성공했습니다.",
                        response
                )
        );
    }

    @GetMapping("/processing-groups/{processingGroupId}")
    public ResponseEntity<ApiResponse<DocumentProcessingGroupResponseDto>> getProcessingGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long processingGroupId
    ) {
        DocumentProcessingGroupResponseDto response =
                ailearnDocumentService.getProcessingGroup(
                        userDetails.getId(),
                        processingGroupId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "문서 처리 상태 조회에 성공했습니다.",
                        response
                )
        );
    }
}