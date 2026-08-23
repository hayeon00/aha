package com.aha.domain.learningnote.controller;

import com.aha.domain.learningnote.dto.response.LearningNoteCoverageResponse;
import com.aha.domain.learningnote.dto.response.LearningNoteCreateResponseDto;
import com.aha.domain.learningnote.dto.response.LearningNoteDetailResponseDto;
import com.aha.domain.learningnote.dto.response.LearningNoteSummaryResponseDto;
import com.aha.domain.learningnote.service.LearningNoteCreateService;
import com.aha.domain.learningnote.service.LearningNoteDeletionService;
import com.aha.domain.learningnote.service.LearningNoteService;
import com.aha.domain.learningnote.service.coverage.LearningNoteCoverageService;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning-notes")
public class LearningNoteCoverageController {

    private final LearningNoteCreateService learningNoteCreateService;
    private final LearningNoteCoverageService learningNoteCoverageService;
    private final LearningNoteService learningNoteService;
    private final LearningNoteDeletionService learningNoteDeletionService;


    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LearningNoteCreateResponseDto>> uploadDocument(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("userExamId") Long userExamId,
            @RequestParam("title") String title,
            @RequestPart("file") MultipartFile file
    ) {
        LearningNoteCreateResponseDto response =
                learningNoteCreateService.create(
                        user.getId(),
                        userExamId,
                        title,
                        file
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        ApiResponse.success(
                                202,
                                "학습노트 생성 요청이 접수되었습니다.",
                                response
                        )
                );
    }

    @GetMapping("/{learningNoteId}/coverage")
    public ResponseEntity<ApiResponse<LearningNoteCoverageResponse>> getCoverage(
            @PathVariable Long learningNoteId
    ) {
        LearningNoteCoverageResponse response =
                learningNoteCoverageService.getCoverage(
                        learningNoteId
                );

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "표지 조회 성공",
                response
                )
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningNoteSummaryResponseDto>>> getCompletedNotes(
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        List<LearningNoteSummaryResponseDto> response =
                learningNoteService.getCompletedNotes(user.getId());

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "완료된 학습노트 조회 성공",
                response

        ));
    }

    @GetMapping("/{learningNoteId}")
    public ResponseEntity<ApiResponse<LearningNoteDetailResponseDto>> getCompletedNote(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long learningNoteId
    ) {

        LearningNoteDetailResponseDto response =
                learningNoteService.getCompletedNote(
                        user.getId(),
                        learningNoteId
                );

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "학습노트 상세 조회 성공",
                response

        ));
    }

    @DeleteMapping("/{learningNoteId}")
    public ResponseEntity<ApiResponse<Void>> deleteCompletedNote(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long learningNoteId
    ) {
        learningNoteDeletionService.deleteCompletedNote(user.getId(), learningNoteId);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "학습노트 삭제 성공",
                null
        ));
    }




}
