package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.response.DocumentUploadResponseDto;
import com.aha.domain.ailearn.document.service.AiLearnDocumentService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ailearn/documents")
@RequiredArgsConstructor
public class AiLearnDocumentController {

    private final AiLearnDocumentService ailearnDocumentService;

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentUploadResponseDto>> uploadDocument(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("userExamId") Long userExamId,
            @RequestPart("file") MultipartFile file
    ) {
        DocumentUploadResponseDto response =
                ailearnDocumentService.uploadDocument(
                        userDetails.getId(),
                        userExamId,
                        file
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "학습 문서 업로드에 성공했습니다.",
                        response
                )
        );
    }
}