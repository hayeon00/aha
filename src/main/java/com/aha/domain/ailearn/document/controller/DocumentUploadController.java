package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.upload.response.BatchUploadResponseDto;
import com.aha.domain.ailearn.document.service.upload.DocumentUploadService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/v1/ai-learning/document-uploads")
@RequiredArgsConstructor
public class DocumentUploadController {

    private final DocumentUploadService documentUploadService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<BatchUploadResponseDto>>
    uploadDocuments(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @RequestParam("userExamId")
            Long userExamId,

            @RequestPart("files")
            List<MultipartFile> files
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
                                HttpStatus.ACCEPTED.value(),
                                "학습 문서 업로드가 접수되었습니다.",
                                response
                        )
                );
    }
}