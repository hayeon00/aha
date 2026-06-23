package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.api.response.DocumentBatchUploadResponseDto;
import com.aha.domain.ailearn.document.dto.api.response.DocumentProcessingGroupResponseDto;
import com.aha.domain.ailearn.document.service.upload.AiLearnDocumentService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-learning/documents")
@RequiredArgsConstructor
public class AiLearnDocumentController {

    private final AiLearnDocumentService aiLearnDocumentService;

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<DocumentBatchUploadResponseDto>> uploadDocumentsBatch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("userExamId") Long userExamId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        DocumentBatchUploadResponseDto response =
                aiLearnDocumentService.uploadDocumentsBatch(
                        userDetails.getId(),
                        userExamId,
                        files
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        202,
                        "학습 문서 업로드가 접수되었습니다.",
                        response
                )
        );
    }

    /*
    @GetMapping("/processing-groups/{processingGroupId}")
    public ResponseEntity<ApiResponse<DocumentProcessingGroupResponseDto>> getProcessingGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long processingGroupId
    ) {
        DocumentProcessingGroupResponseDto response =
                aiLearnDocumentService.getProcessingGroup(
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

     */
}