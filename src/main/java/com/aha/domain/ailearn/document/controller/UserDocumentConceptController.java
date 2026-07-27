package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.content.response.DocumentConceptDashboardResponseDto;
import com.aha.domain.ailearn.document.dto.content.request.ConceptGenerationRequestDto;
import com.aha.domain.ailearn.document.dto.content.request.ConceptUpdateRequestDto;
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
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long userExamId) {

        return ok(
                "업로드 한 문서 목록을 조회했습니다.",
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
            @PathVariable Long documentId, @PathVariable Long tocId,
            @RequestBody(required = false) ConceptGenerationRequestDto request) {
        return ok("AI 보완 설명을 생성했습니다.",
                conceptService.generate(user.getId(), documentId, tocId,
                        request == null ? null : request.prompt()));
    }

    @PatchMapping("/{documentId}/toc/{tocId}/concept")
    public ResponseEntity<ApiResponse<UserDocumentConceptResponseDto>> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long documentId,
            @PathVariable Long tocId,
            @RequestBody ConceptUpdateRequestDto request) {
        return ok("개념 설명을 저장했습니다.",
                conceptService.update(user.getId(), documentId, tocId, request.content()));
    }

    @PostMapping("/{documentId}/generate-missing-concepts")
    public ResponseEntity<ApiResponse<List<UserDocumentConceptResponseDto>>> generateMissing(
            @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long documentId) {
        return ok("빠진 개념을 모두 생성했습니다.",
                conceptService.generateMissing(user.getId(), documentId));
    }

    @PostMapping("/{documentId}/learning-note")
    public ResponseEntity<ApiResponse<Void>> createLearningNote(
            @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long documentId) {
        conceptService.createLearningNote(user.getId(), documentId);
        return ok("학습노트를 만들었습니다.", null);
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, data));
    }
}
