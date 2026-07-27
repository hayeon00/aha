package com.aha.domain.notestudio.controller;

import com.aha.domain.notestudio.document.dto.content.response.LearningNoteSummaryResponseDto;
import com.aha.domain.notestudio.document.dto.content.request.ConceptGenerationRequestDto;
import com.aha.domain.notestudio.document.dto.content.request.ConceptUpdateRequestDto;
import com.aha.domain.notestudio.document.dto.content.response.UserDocumentConceptResponseDto;
import com.aha.domain.notestudio.document.service.content.LearningNoteService;
import com.aha.domain.notestudio.document.service.content.UserDocumentConceptService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LearningNoteController {

    private final LearningNoteService learningNoteService;
    private final UserDocumentConceptService conceptService;

    @GetMapping("/api/v1/learning-notes")
    public ResponseEntity<ApiResponse<List<LearningNoteSummaryResponseDto>>> getCompletedNotes(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(
                200,
                "완료된 학습노트를 조회했습니다.",
                learningNoteService.getCompletedNotes(user.getId())));
    }

    @PostMapping("/api/documents/{documentId}/learning-note")
    public ResponseEntity<ApiResponse<LearningNoteSummaryResponseDto>> createLearningNote(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long documentId) {
        return ok("학습노트를 만들었습니다.",
                learningNoteService.createFromDocument(user.getId(), documentId));
    }

    @PostMapping("/api/documents/{documentId}/toc/{tocId}/generate-concept")
    public ResponseEntity<ApiResponse<UserDocumentConceptResponseDto>> generateConcept(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long documentId,
            @PathVariable Long tocId,
            @RequestBody(required = false) ConceptGenerationRequestDto request) {
        return ok("AI 보완 설명을 생성했습니다.",
                conceptService.generate(user.getId(), documentId, tocId,
                        request == null ? null : request.prompt()));
    }

    @PatchMapping("/api/documents/{documentId}/toc/{tocId}/concept")
    public ResponseEntity<ApiResponse<UserDocumentConceptResponseDto>> updateConcept(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long documentId,
            @PathVariable Long tocId,
            @RequestBody ConceptUpdateRequestDto request) {
        return ok("개념 설명을 저장했습니다.",
                conceptService.update(user.getId(), documentId, tocId, request.content()));
    }

    @PostMapping("/api/documents/{documentId}/generate-missing-concepts")
    public ResponseEntity<ApiResponse<List<UserDocumentConceptResponseDto>>> generateMissingConcepts(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long documentId) {
        return ok("빠진 개념을 모두 생성했습니다.",
                conceptService.generateMissing(user.getId(), documentId));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(200, message, data));
    }
}
