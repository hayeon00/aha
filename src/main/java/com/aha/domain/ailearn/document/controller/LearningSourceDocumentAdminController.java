package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.response.*;
import com.aha.domain.ailearn.document.service.DocumentExtractionService;
import com.aha.domain.ailearn.document.service.DocumentStructuringService;
import com.aha.domain.ailearn.document.service.GeneratedLearningContentService;
import com.aha.domain.ailearn.document.service.LearningSourceDocumentService;
import com.aha.domain.ailearn.document.type.SourceType;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
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
    private final GeneratedLearningContentService generatedLearningContentService;

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
        Long userId = getLoginUserId(userDetails);

        LearningSourceDocumentUploadResponseDto response =
                sourceDocumentService.uploadDocument(
                        examId,
                        examPartId,
                        examScopeNodeId,
                        title,
                        sourceType,
                        description,
                        file,
                        userId
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
        Long userId = getLoginUserId(userDetails);

        DocumentExtractionResponseDto response =
                documentExtractionService.extractText(
                        sourceDocumentId,
                        userId
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "학습 원본문서 텍스트 추출에 성공했습니다.",
                response
        );
    }

    @PostMapping("/{sourceDocumentId}/structure")
    public ApiResponse<DocumentStructuringResponseDto> structureDocumentContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sourceDocumentId
    ) {
        Long userId = getLoginUserId(userDetails);

        DocumentStructuringResponseDto response =
                documentStructuringService.structureContent(
                        sourceDocumentId,
                        userId
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "학습 원본문서 구조화에 성공했습니다.",
                response
        );
    }

    @GetMapping("/{sourceDocumentId}/generated-bodies")
    public ApiResponse<GeneratedLearningContentBodyListResponseDto> getGeneratedBodies(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sourceDocumentId
    ) {
        getLoginUserId(userDetails);

        GeneratedLearningContentBodyListResponseDto response =
                generatedLearningContentService.getGeneratedBodies(sourceDocumentId);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AI 구조화 본문 조회에 성공했습니다.",
                response
        );
    }

    @PostMapping("/{sourceDocumentId}/publish")
    public ApiResponse<LearningContentPublishResponseDto> publishGeneratedBodies(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sourceDocumentId
    ) {
        getLoginUserId(userDetails);

        LearningContentPublishResponseDto response =
                generatedLearningContentService.publishGeneratedBodies(sourceDocumentId);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AI 구조화 본문 게시에 성공했습니다.",
                response
        );
    }






    private Long getLoginUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return userDetails.getId();
    }
}