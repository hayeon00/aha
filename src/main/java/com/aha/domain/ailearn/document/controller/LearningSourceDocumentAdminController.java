package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.response.DocumentExtractionResponseDto;
import com.aha.domain.ailearn.document.dto.response.DocumentStructuringResponse;
import com.aha.domain.ailearn.document.dto.response.LearningSourceDocumentUploadResponseDto;
import com.aha.domain.ailearn.document.service.DocumentExtractionService;
import com.aha.domain.ailearn.document.service.DocumentStructuringService;
import com.aha.domain.ailearn.document.service.LearningSourceDocumentService;
import com.aha.domain.ailearn.document.type.SourceType;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/learning-source-documents")
@RequiredArgsConstructor
public class LearningSourceDocumentAdminController {

    private final LearningSourceDocumentService sourceDocumentService;
    private final DocumentExtractionService documentExtractionService;
    private final DocumentStructuringService documentStructuringService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<LearningSourceDocumentUploadResponseDto> uploadDocument(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long examId,
            @RequestParam(required = false) Long examPartId,
            @RequestParam(required = false) Long examScopeNodeId,
            @RequestParam String title,
            @RequestParam SourceType sourceType,
            @RequestParam(required = false) String description,
            @RequestPart MultipartFile file
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인 사용자 정보가 없습니다. Authorization 헤더를 확인해주세요.");
        }

        LearningSourceDocumentUploadResponseDto response =
                sourceDocumentService.uploadDocument(
                        examId,
                        examPartId,
                        examScopeNodeId,
                        title,
                        sourceType,
                        description,
                        file,
                        userDetails.getId()
                );

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "학습 원본문서 업로드에 성공했습니다.",
                response
        );
    }

    @PostMapping("/{sourceDocumentId}/extract")
    public ApiResponse<DocumentExtractionResponseDto> extractDocumentText(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sourceDocumentId
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인 사용자 정보가 없습니다. Authorization 헤더를 확인해주세요.");
        }

        DocumentExtractionResponseDto response =
                documentExtractionService.extractText(
                        sourceDocumentId,
                        userDetails.getId()
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "학습 원본문서 텍스트 추출에 성공했습니다.",
                response
        );
    }


    @PostMapping("/{sourceDocumentId}/structure")
    public ApiResponse<DocumentStructuringResponse> structureDocumentContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sourceDocumentId
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인 사용자 정보가 없습니다. Authorization 헤더를 확인해주세요.");
        }

        DocumentStructuringResponse response =
                documentStructuringService.structureContent(
                        sourceDocumentId,
                        userDetails.getId()
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "학습 원본문서 구조화에 성공했습니다.",
                response
        );
    }




}