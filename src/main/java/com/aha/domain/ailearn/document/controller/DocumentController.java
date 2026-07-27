package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.upload.response.BatchUploadResponseDto;
import com.aha.domain.ailearn.document.service.DocumentService;
import com.aha.domain.ailearn.document.service.upload.DocumentUploadService;
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
@RequestMapping("/api/v1/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentUploadService documentUploadService;
    private final DocumentService documentService;


    @PostMapping("/upload")
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

    @DeleteMapping("/{documentId}")
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
}
