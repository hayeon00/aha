package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.content.response.DocumentConceptDashboardResponseDto;
import com.aha.domain.ailearn.document.dto.content.response.OwnedDocumentResponseDto;
import com.aha.domain.ailearn.document.dto.content.response.UserDocumentConceptResponseDto;
import com.aha.domain.ailearn.document.service.content.UserDocumentConceptService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class UserDocumentConceptController {
    private final UserDocumentConceptService conceptService;

    @GetMapping("/user-exams/{userExamId}")
    public ResponseEntity<ApiResponse<List<OwnedDocumentResponseDto>>> getDocuments(
            @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long userExamId) {
        return ok("내 문서 목록을 조회했습니다.",
                conceptService.getOwnedDocuments(user.getId(), userExamId));
    }

    @GetMapping("/{documentId}/concepts")
    public ResponseEntity<ApiResponse<DocumentConceptDashboardResponseDto>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long documentId) {
        return ok("문서별 개념 현황을 조회했습니다.",
                conceptService.getDashboard(user.getId(), documentId));
    }

    @PostMapping("/{documentId}/toc/{tocId}/generate-concept")
    public ResponseEntity<ApiResponse<UserDocumentConceptResponseDto>> generate(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long documentId, @PathVariable Long tocId) {
        return ok("AI 보완 설명을 생성했습니다.",
                conceptService.generate(user.getId(), documentId, tocId));
    }

    @PostMapping("/{documentId}/generate-missing-concepts")
    public ResponseEntity<ApiResponse<List<UserDocumentConceptResponseDto>>> generateMissing(
            @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long documentId) {
        return ok("빠진 개념을 모두 생성했습니다.",
                conceptService.generateMissing(user.getId(), documentId));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, data));
    }
}
